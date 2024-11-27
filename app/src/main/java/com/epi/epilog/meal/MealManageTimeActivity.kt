package com.epi.epilog.meal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.MealManageResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.databinding.ManageMealTimeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealManageTimeActivity : AppCompatActivity() {

    private lateinit var binding: ManageMealTimeBinding
    private lateinit var mealAdapter: MealTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 설정
        binding = ManageMealTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 설정
        setupRecyclerView()

        // 서버 데이터 가져오기
        fetchMealsFromServer()

        // 식사 추가 버튼 이벤트
        binding.addMealtimeButton.setOnClickListener {
            // 더미 데이터로 새로운 식사 추가
            val newMeal = MealManageResponse(3, "저녁식사 18:30", true)
            mealAdapter.addMeal(newMeal)
        }
    }

    private fun setupRecyclerView() {
        mealAdapter = MealTimeAdapter(mutableListOf())
        binding.mealtimeContainer.apply {
            layoutManager = LinearLayoutManager(this@MealManageTimeActivity)
            adapter = mealAdapter
        }
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", "") ?: ""
    }


    private fun fetchMealsFromServer() {

        val token = "Bearer " + getTokenFromSession()
        Log.d("MealList","token : $token")

        // 서버 요청
        RetrofitClient.retrofitService.getMealChecklist(token).enqueue(object :
            Callback<List<MealManageResponse>> {
            override fun onResponse(
                call: Call<List<MealManageResponse>>,
                response: Response<List<MealManageResponse>>
            ) {
                Log.d("API Response", "Code: ${response.code()}, Message: ${response.message()}")
                if (response.isSuccessful) {
                    val mealList = response.body() ?: emptyList()
                    // 로그로 수신된 리스트 출력
                    Log.d("MealList", "Fetched meals: $mealList")
                    mealAdapter.updateMeals(mealList)
                } else {
                    Toast.makeText(
                        this@MealManageTimeActivity,
                        "Failed to fetch data: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<MealManageResponse>>, t: Throwable) {
                Log.e("MealManageTimeActivity", "Error fetching meals", t)
                Toast.makeText(
                    this@MealManageTimeActivity,
                    "Error fetching meals: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

private fun <T> Call<T>.enqueue(callback: Callback<List<T>>) {

}

class MealTimeAdapter(private val meals: MutableList<MealManageResponse>) :
    RecyclerView.Adapter<MealTimeAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealNameTimeTextView: TextView = view.findViewById(R.id.manage_mealNameTime)
        val switchMealAlarm: Switch = view.findViewById(R.id.switch_meal_alarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.meal_time_add_item, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealNameTimeTextView.text = meal.title
        holder.switchMealAlarm.isChecked = meal.isAlarm

        // Switch 상태 변경 이벤트 처리
        holder.switchMealAlarm.setOnCheckedChangeListener { _, isChecked ->
            meal.isAlarm = isChecked
        }
    }

    override fun getItemCount(): Int = meals.size

    // 새로운 데이터 추가
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

