package com.epi.epilog.meal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.ApiResponse
import com.epi.epilog.api.MealManageAddRequest
import com.epi.epilog.api.MealManageResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.databinding.ManageMealTime2Binding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MealManageTime2Activity : AppCompatActivity() {

        private lateinit var binding: ManageMealTime2Binding
        private lateinit var mealAdapter: MealTimeAddAdapter

        private var selectedMealType: String = "아침식사" // 기본 값
        private var selectedTime: String = "07:30" // 기본 값

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                // ViewBinding 설정
                binding = ManageMealTime2Binding.inflate(layoutInflater)
                setContentView(binding.root)

                // RecyclerView 설정
                setupRecyclerView()

                // 서버에서 기존 데이터를 불러오기
                fetchMealsFromServer()

                // TimePicker 리스너 설정
                setupTimePicker()

                // Spinner(드롭다운) 설정
                setupSpinner()

                // "식사 시간 추가하기" 버튼 클릭 이벤트
                binding.mealTimeAddBtn.setOnClickListener {
                        addMealTime()
                }
        }

        private fun setupRecyclerView() {
                mealAdapter = MealTimeAddAdapter(mutableListOf())
                binding.mealtimeContainer.apply {
                        layoutManager = LinearLayoutManager(this@MealManageTime2Activity)
                        adapter = mealAdapter
                }
        }

        private fun fetchMealsFromServer() {
                val token = "Bearer " + getTokenFromSession()
                // 서버 요청
                RetrofitClient.retrofitService.getMealChecklist(token).enqueue(object :
                        Callback<List<MealManageResponse>> {
                        override fun onResponse(
                                call: Call<List<MealManageResponse>>,
                                response: Response<List<MealManageResponse>>
                        ) {
                                if (response.isSuccessful) {
                                        val mealList = response.body() ?: emptyList()
                                        // 기존 데이터를 RecyclerView에 추가
                                        mealAdapter.updateMeals(mealList)
                                } else {
                                        Toast.makeText(
                                                this@MealManageTime2Activity,
                                                "Failed to fetch data: ${response.message()}",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                }
                        }

                        override fun onFailure(call: Call<List<MealManageResponse>>, t: Throwable) {
                                Log.e("MealManageTime2Activity", "Error fetching meals", t)
                                Toast.makeText(
                                        this@MealManageTime2Activity,
                                        "Error fetching meals: ${t.message}",
                                        Toast.LENGTH_SHORT
                                ).show()
                        }
                })
        }

        private fun setupTimePicker() {
                binding.mealTimeTimepicker.setOnTimeChangedListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                        selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                }
        }

        private fun setupSpinner() {
                val mealTypes = listOf("아침식사", "점심식사", "저녁식사")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mealTypes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.mealTimeDropdown.adapter = adapter

                binding.mealTimeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                selectedMealType = mealTypes[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Do nothing
                        }
                }
        }

        private fun addMealTime() {
                val isAlarmEnabled = true // 기본 값으로 설정

                // 단일 객체 생성
                val newMeal = MealManageAddRequest(
                        time = selectedTime,
                        mealType = selectedMealType,
                        isAlarm = isAlarmEnabled
                )
                val mealList = listOf(newMeal)

                // 요청 데이터를 JSON으로 변환 후 로그 출력
                val gson = Gson()
                val jsonRequest = gson.toJson(mealList)
                Log.d("MealManageTime2", "Request Body: $jsonRequest")

                // RecyclerView에 데이터 추가
                mealAdapter.addMeal(newMeal.toResponse()) // RecyclerView 업데이트

                // 서버로 POST 요청
                val token = "Bearer " + getTokenFromSession()
                RetrofitClient.retrofitService.MealManageAddTime(token, mealList).enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                                if (response.isSuccessful) {

                                        // 권한 확인 및 설정 화면 이동 코드
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                val alarmManager = getSystemService(AlarmManager::class.java)
                                                if (!alarmManager.canScheduleExactAlarms()) {
                                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                                data = Uri.parse("package:$packageName")
                                                        }
                                                        startActivity(intent)
                                                        Toast.makeText(
                                                                this@MealManageTime2Activity,
                                                                "정확한 알람을 설정하려면 권한을 허용해주세요.",
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                                        return // 권한 허용 후 다시 알람을 설정하도록 종료
                                                }
                                        }

                                        if (isAlarmEnabled) {
                                                Log.d("AlarmDebug", "Meal added successfully: $newMeal")
                                                Log.d("AlarmDebug", "Setting alarm for $newMeal")
                                                setMealAlarm(newMeal)
                                        }
                                        Toast.makeText(this@MealManageTime2Activity, "식사 시간이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                        Log.e("MealManageTime2", "Request failed! Response code: ${response.code()}")
                                        Log.e("MealManageTime2", "Response error body: ${response.errorBody()?.string()}")
                                        Toast.makeText(this@MealManageTime2Activity, "추가 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                                }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                Log.e("MealManageTime2", "Request failed due to network error", t)
                                Toast.makeText(this@MealManageTime2Activity, "서버 요청 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                })
        }

        private fun setMealAlarm(meal: MealManageAddRequest) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, MealAlarmReceiver::class.java).apply {
                        putExtra("mealType", meal.mealType)
                        putExtra("time", meal.time)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        meal.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val parts = meal.time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) {
                                add(Calendar.DAY_OF_YEAR, 1)
                        }
                }

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                )

                Toast.makeText(this, "${meal.mealType} 알람이 ${meal.time}에 설정되었습니다.", Toast.LENGTH_SHORT).show()
                Log.d("AlarmDebug", "Alarm set for ${calendar.time} with intent: ${intent.extras}")
        }




        private fun getTokenFromSession(): String {
                val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                return sharedPreferences.getString("AuthToken", "") ?: ""
        }
}


class MealTimeAddAdapter(private val meals: MutableList<MealManageResponse>) :
        RecyclerView.Adapter<MealTimeAddAdapter.MealViewHolder>() {

        class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val mealNameTimeTextView: TextView = view.findViewById(R.id.manage_mealNameTime)
                val switchMealAlarm: Switch = view.findViewById(R.id.switch_meal_alarm)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.meal_time_add_item, parent, false)
                return MealViewHolder(view)
        }

        override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
                val meal = meals[position]

                // title에서 mealType과 time 분리 (예: "아침식사 07:30")
                val parts = meal.title.split(" ") // 공백으로 분리
                val mealType = parts[0] // "아침식사"
                val time = parts.getOrElse(1) { "" } // "07:30"

                holder.mealNameTimeTextView.text = "$mealType $time"
                holder.switchMealAlarm.isChecked = meal.isAlarm

                // Switch 상태 변경 이벤트 처리
                holder.switchMealAlarm.setOnCheckedChangeListener { _, isChecked ->
                        meal.isAlarm = isChecked
                }
        }

        override fun getItemCount(): Int = meals.size

        fun addMeal(meal: MealManageResponse) {
                meals.add(meal)
                notifyItemInserted(meals.size - 1)
        }

        // 기존 데이터를 업데이트
        fun updateMeals(newMeals: List<MealManageResponse>) {
                meals.clear()
                meals.addAll(newMeals)
                notifyDataSetChanged()
        }
}

fun MealManageAddRequest.toResponse(): MealManageResponse {
        return MealManageResponse(
                id = 0, // 새 항목이므로 ID는 임의로 설정
                title = "$mealType $time",
                isAlarm = isAlarm
        )
}

