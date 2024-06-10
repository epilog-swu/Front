package com.epi.epilog.presentation

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.api.MealCheckItem
import com.epi.epilog.presentation.theme.api.MealCheckListResponse
import com.epi.epilog.presentation.theme.api.MealUpdateInfo
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MealActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var retrofitService: RetrofitService
    private var myDataset: MutableList<MealCheckItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)
        setupRetrofit()
        val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let { LocalDate.parse(it) }
        if (selectedDate != null) {
            getMealCheckList(selectedDate.toString())
        } else {
            Log.e("MealActivity", "No date received")
        }
    }

    private fun setupRetrofit() {
        val baseUrl = "http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/"
        retrofitService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }

    private fun getMealCheckList(date: String) {
        val authToken = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("AuthToken", null)
        if (authToken != null) {
            retrofitService.getMealCheckList(date, "Bearer $authToken")
                .enqueue(object : Callback<MealCheckListResponse> {
                    override fun onResponse(
                        call: Call<MealCheckListResponse>,
                        response: Response<MealCheckListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            myDataset.clear()
                            myDataset.addAll(response.body()!!.checklist)
                            setupRecyclerView()
                            Log.d("MealActivity", "success")
                            myDataset.forEach { item ->
                                Log.d(
                                    "MealActivity",
                                    "ID: ${item.id}, Title: ${item.title}, State: ${item.state}, IsComplete: ${item.isComplete}"
                                )
                            }
                        } else {
                            Log.e(
                                "MealActivity",
                                "Failed to load data: " + response.errorBody()?.string()
                            )
                        }
                    }

                    override fun onFailure(call: Call<MealCheckListResponse>, t: Throwable) {
                        Log.e("MealActivity", "API call failed: ${t.message}")
                    }
                })
        } else {
            Log.e("MealActivity", "Auth token is missing")
        }
    }

    private fun setupRecyclerView() {
        viewAdapter = MyAdapter(myDataset, this)
        recyclerView = findViewById<RecyclerView>(R.id.wearable_recycler_view_meal).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MealActivity)
            adapter = viewAdapter
        }
    }

    class MyAdapter(
        private val myDataset: MutableList<MealCheckItem>,
        private val context: MealActivity // 컨텍스트를 Activity로 명확히 지정
    ) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        class MyViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val checkBox = LayoutInflater.from(parent.context)
                .inflate(R.layout.checkbox_item, parent, false) as CheckBox
            return MyViewHolder(checkBox)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = myDataset[position]
            holder.checkBox.text = when (item.state) {
                State.식사함, State.건너뜀 -> "${item.title}(완료)"
                State.상태없음 -> "${item.title}(예정)"
            }
            holder.checkBox.isChecked = item.state == State.식사함 || item.state == State.건너뜀

            val currentDateTime = LocalDateTime.now()
            val goalTime = LocalDateTime.parse(item.goalTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            if (item.state == State.상태없음) {
                if (goalTime.isBefore(currentDateTime)) {
                    holder.checkBox.background = ContextCompat.getDrawable(context, R.drawable.checkbox_selector_red)
                } else {
                    holder.checkBox.background = ContextCompat.getDrawable(context, R.drawable.checkbox_selector_yellow)
                }
            }

            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.isChecked = !isChecked // 상태를 변경하지 않도록 설정
                showDialog(item)
            }
        }

        override fun getItemCount() = myDataset.size

        private fun showDialog(item: MealCheckItem) {
            val builder = AlertDialog.Builder(context)
            val inflater = context.layoutInflater
            val dialogView = inflater.inflate(R.layout.activity_checklist_dialog, null)
            builder.setView(dialogView)

            val dialog = builder.create()
            dialogView.findViewById<Button>(R.id.dialog_button_yes).setOnClickListener {
                updateMealStatus(item.id, getCurrentDateTime(), "식사함")
                item.state = State.식사함
                notifyItemChanged(myDataset.indexOf(item))
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_no).setOnClickListener {
                updateMealStatus(item.id, item.goalTime, "식사함")
                item.state = State.식사함
                notifyItemChanged(myDataset.indexOf(item))
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_pass).setOnClickListener {
                updateMealStatus(item.id, getCurrentDateTime(), "건너뜀")
                item.state = State.건너뜀
                notifyItemChanged(myDataset.indexOf(item))
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun updateMealStatus(chklistId: Int, time: String, status: String) {
            val authToken = context.getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("AuthToken", null)
            if (authToken != null) {
                val updateInfo = MealUpdateInfo(time, status)
                context.retrofitService.updateMealStatus(chklistId, "Bearer $authToken", updateInfo)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                Log.d("MealActivity", "Status Updated Successfully: ${response.body()?.message}")

                            } else {
                                Log.e("MealActivity", "Failed to update status: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Log.e("MealActivity", "API call failed: ${t.message}")
                        }
                    })
            } else {
                Log.e("MealActivity", "Auth token is missing")
            }
        }

        private fun getCurrentDateTime(): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return LocalDateTime.now().format(formatter)
        }
    }
}
