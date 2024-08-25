package com.epi.epilog.presentation.medicine

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.provider.Settings
import android.widget.Toast
import com.epi.epilog.R
import com.epi.epilog.presentation.UpdateChecklistStatusRequest


class MedicineActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var retrofitService: RetrofitService
    private var checklistData: MedicineCheckListDatas? = null

    private val alarmManager: AlarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }


    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun saveChecklistData() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ChecklistData", Gson().toJson(checklistData))
        editor.apply()
    }

    private fun loadChecklistData(selectedDate: String): MedicineCheckListDatas? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val checklistJson = sharedPreferences.getString("ChecklistData", null)
        return if (checklistJson != null) {
            val data = Gson().fromJson(checklistJson, MedicineCheckListDatas::class.java)
            if (data.date == selectedDate) { // 날짜 비교
                data
            } else {
                null
            }
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        // 알림 채널 생성
        createNotificationChannel()

        // RecyclerView와 TextView 참조
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.wearable_recycler_view_medicine).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        val noChecklistMessage: TextView = findViewById(R.id.no_checklist_message)

        // Retrofit 초기화
        initializeRetrofit()

        // 오늘의 날짜 정보 받기
        val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let {
            LocalDate.parse(it)
        }
        Log.d("MedicineActivity", selectedDate.toString())

        checklistData = loadChecklistData(selectedDate.toString())

        if (checklistData != null) {
            // 체크리스트 데이터가 있을 경우 RecyclerView 어댑터 설정 및 TextView 숨기기
            viewAdapter = MyAdapter(this@MedicineActivity, checklistData)
            recyclerView.adapter = viewAdapter
            recyclerView.visibility = View.VISIBLE
            noChecklistMessage.visibility = View.GONE

            // 알람 설정 추가
            checklistData?.checklist?.forEach { item ->
                scheduleMedicineNotifications(item)
            }

        } else {
            // 체크리스트 데이터가 없을 경우 서버에서 데이터 가져오기
            val authToken = getTokenFromSession()
            if (authToken.isNullOrEmpty()) {
                Log.e("MedicineActivity", "Auth token is missing")
                return
            }

            if (selectedDate != null) {
                retrofitService.getMedicineChecklist(selectedDate.toString(), "Bearer $authToken").enqueue(object : Callback<MedicineCheckListDatas> {
                    override fun onResponse(call: Call<MedicineCheckListDatas>, response: Response<MedicineCheckListDatas>) {
                        if (response.isSuccessful) {
                            response.body()?.let { dataList ->
                                checklistData = dataList
                                saveChecklistData() // 로컬에 데이터 저장
                                Log.d("MedicineActivity", "Fetched data: $dataList")

                                if (dataList.checklist.isNotEmpty()) {
                                    // 체크리스트 데이터가 있을 경우 RecyclerView 어댑터 설정 및 TextView 숨기기
                                    viewAdapter = MyAdapter(this@MedicineActivity, checklistData)
                                    recyclerView.adapter = viewAdapter
                                    recyclerView.visibility = View.VISIBLE
                                    noChecklistMessage.visibility = View.GONE

                                    // 알람 설정 추가
                                    dataList.checklist.forEach { item ->
                                        scheduleMedicineNotifications(item)
                                    }

                                } else {
                                    // 체크리스트 데이터가 없을 경우 TextView 표시
                                    recyclerView.visibility = View.GONE
                                    noChecklistMessage.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            Log.e("MedicineActivity", "Error in fetching data: ${response.code()} - ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<MedicineCheckListDatas>, t: Throwable) {
                        Log.e("MedicineActivity", "Failed to fetch data", t)
                    }
                })
            } else {
                Log.e("MedicineActivity", "No date received")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "MedicineActivityChannel"
            val channelName = "Medicine Notifications"
            val channelDescription = "Notifications for medicine reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    private fun scheduleMedicineNotifications(item: checklist) {
        try {
            val goalTime = LocalDateTime.parse(item.goalTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val notificationTime = goalTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentTime = System.currentTimeMillis()

            if (notificationTime > currentTime) {
                // 알람 설정
                scheduleNotification(item.id, notificationTime, "${item.title} 복용 시간입니다.")
                Log.d("MedicineActivity", "알람 설정 시간: $notificationTime")
            } else {
                Log.d("MedicineActivity", "과거의 시간에 대한 알람은 설정되지 않습니다: $notificationTime")
            }
        } catch (e: SecurityException) {
            Log.e("MedicineActivity", "Failed to schedule notifications due to missing permission: ${e.message}", e)
            // 권한 요청 로직 추가 가능
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestExactAlarmPermission()
            }
        } catch (e: Exception) {
            Log.e("MedicineActivity", "Failed to schedule notifications: ${e.message}", e)
        }
    }


    private fun scheduleNotification(id: Int, time: Long, message: String) {
        try {
            val intent = Intent(this, MedicineNotificationReceiver::class.java).apply {
                putExtra("notificationId", id)
                putExtra("message", message)
            }
            val pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)

        } catch (e: SecurityException) {
            Log.e("MedicineActivity", "Failed to schedule notification due to missing permission: ${e.message}", e)
            // 권한 요청 로직 추가 가능
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestExactAlarmPermission()
            }
        } catch (e: Exception) {
            Log.e("MedicineActivity", "Failed to schedule notification: ${e.message}", e)
        }
    }



    fun MedicineshowDialog(chklstId: Int, goalTime: String, onStatusChanged: (State, String) -> Unit) {
        Log.d("MedicineActivity", "showDialog called with chklistId: $chklstId")
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_checklist_dialog_2, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        with(builder) {
            val dialog = create()

            dialogView.findViewById<Button>(R.id.dialog_button_yes).setOnClickListener {
                val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                onStatusChanged(State.복용, time)
                updateChecklistStatus(chklstId, State.복용, time)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_no).setOnClickListener {
                onStatusChanged(State.복용, goalTime)
                updateChecklistStatus(chklstId, State.복용, goalTime)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_pass).setOnClickListener {
                val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                onStatusChanged(State.미복용, time)
                updateChecklistStatus(chklstId, State.미복용, time)
                dialog.dismiss()
            }
            dialog.show()
        }
    }


    private fun updateChecklistStatus(chklstId: Int, status: State, time: String) {
        val authToken = getTokenFromSession()
        if (authToken.isNullOrEmpty()) {
            Log.e("MedicineActivity", "Auth token is missing")
            return
        }

        val requestBody = UpdateChecklistStatusRequest(
            time = time,
            status = status
        )

        // 로그 추가
        Log.d("MedicineActivity", "Sending request to update checklist status with id: $chklstId and status: $status and time: $time")

        retrofitService.updateMedicalChecklist(chklstId, requestBody, "Bearer $authToken").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MedicineActivity", "Checklist updated successfully")
                    // response.body() 출력하는 로그 추가
                    Log.d("MedicineActivity", "Response body: ${response.body()?.string()}")
                } else {
                    Log.e("MedicineActivity", "Error in updating checklist: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MedicineActivity", "Failed to update checklist", t)
            }
        })
    }

    class MyAdapter(private val context: MedicineActivity, private val data: MedicineCheckListDatas?) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        class MyViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val checkBox = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_item, parent, false) as CheckBox
            return MyViewHolder(checkBox)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            data?.checklist?.get(position)?.let { checklistItem ->
                holder.checkBox.text = checklistItem.title
                holder.checkBox.isChecked = checklistItem.isComplete

                //background 설정을 위한 변수 설정
                val currentDateTime = LocalDateTime.now()
                val goalTime = LocalDateTime.parse(checklistItem.goalTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                if(goalTime.isBefore(currentDateTime)){
                    holder.checkBox.background = ContextCompat.getDrawable(context,
                        R.drawable.checkbox_selector_red
                    )
                } else {
                    holder.checkBox.background = ContextCompat.getDrawable(context,
                        R.drawable.checkbox_selector_yellow
                    )
                }


                holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {  // 사용자가 직접 클릭한 경우에만 처리
                        if (isChecked) {
                            // 체크박스가 체크되었을 때 다이얼로그 표시
                            holder.checkBox.isChecked = checklistItem.isComplete  // 상태 변경을 일단 취소
                            (context as MedicineActivity).MedicineshowDialog(checklistItem.id, checklistItem.goalTime) { state, time ->
                                checklistItem.isComplete = state == State.복용
                                holder.checkBox.isChecked = checklistItem.isComplete  // 다이얼로그 응답에 따라 체크박스 상태 변경
                                (context as MedicineActivity).saveChecklistData() // 상태 변경 후 데이터 저장
                            }
                        } else {
                            // 체크박스가 해제되었을 때 바로 서버로 데이터 전송
                            checklistItem.isComplete = false
                            (context as MedicineActivity).updateChecklistStatus(checklistItem.id,
                                State.상태없음, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            (context as MedicineActivity).saveChecklistData() // 상태 변경 후 데이터 저장
                            Toast.makeText(context, "취소되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        override fun getItemCount() = data?.checklist?.size ?: 0
    }
}
