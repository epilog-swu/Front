package com.epi.epilog.presentation

import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
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
import java.time.format.DateTimeFormatter

class MedicineActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var retrofitService: RetrofitService
    private var checklistData: MedicineCheckListDatas? = null

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

    private fun loadChecklistData(): MedicineCheckListDatas? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val checklistJson = sharedPreferences.getString("ChecklistData", null)
        return if (checklistJson != null) {
            Gson().fromJson(checklistJson, MedicineCheckListDatas::class.java)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        // RecyclerView 설정
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.wearable_recycler_view_medicine).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        // Retrofit 초기화
        initializeRetrofit()

        // 로컬 데이터 로드
        checklistData = loadChecklistData()

        if (checklistData != null) {
            // RecyclerView 어댑터 설정
            viewAdapter = MyAdapter(this@MedicineActivity, checklistData)
            recyclerView.adapter = viewAdapter
        } else {
            // 오늘의 날짜 정보 받기
            val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let {
                LocalDate.parse(it)
            }
            Log.d("MedicineActivity", selectedDate.toString())

            val authToken = getTokenFromSession()
            if (authToken.isNullOrEmpty()) {
                Log.e("MedicineActivity", "Auth token is missing")
                return
            }

            if (selectedDate != null) {
                // Retrofit 응답 처리
                retrofitService.getMedicineChecklist(selectedDate.toString(), "Bearer $authToken").enqueue(object : Callback<MedicineCheckListDatas> {
                    override fun onResponse(call: Call<MedicineCheckListDatas>, response: Response<MedicineCheckListDatas>) {
                        if (response.isSuccessful) {
                            response.body()?.let { dataList ->
                                checklistData = dataList
                                saveChecklistData() // 로컬에 데이터 저장
                                // response.body() 출력하는 로그 추가
                                Log.d("MedicineActivity", "Fetched data: $dataList")

                                // RecyclerView 어댑터 설정
                                viewAdapter = MyAdapter(this@MedicineActivity, checklistData)
                                recyclerView.adapter = viewAdapter
                                recyclerView.invalidate() // 데이터 변경 후 UI 갱신
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
                            (context as MedicineActivity).updateChecklistStatus(checklistItem.id, State.상태없음, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            (context as MedicineActivity).saveChecklistData() // 상태 변경 후 데이터 저장
                        }
                    }
                }
            }
        }

        override fun getItemCount() = data?.checklist?.size ?: 0
    }
}
