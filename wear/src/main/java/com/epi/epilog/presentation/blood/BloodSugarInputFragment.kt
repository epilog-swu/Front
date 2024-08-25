package com.epi.epilog.presentation.blood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.MainActivity
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BloodSugarInputFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blood_sugar_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollView = activity?.findViewById<ScrollView>(R.id.blood_sugar_scroll)
        scrollView?.scrollTo(0, 0)

        val date = activity?.intent?.getStringExtra("SELECTED_DATE") ?: ""
        val occurrenceType = arguments?.getString("occurrenceType") ?: ""
        val bloodSugarInput = view.findViewById<EditText>(R.id.bloodSugarLevelInput)
        val submitButton = view.findViewById<Button>(R.id.blood_sugar_submit_btn)
        submitButton.isEnabled = false

        // TextWatcher를 사용하여 텍스트 변경 감지 및 색상 변경
        bloodSugarInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val bloodSugarValue: Int?
                try {
                    bloodSugarValue = s.toString().toInt()
                    if (bloodSugarValue > 350) {
                        bloodSugarInput.setTextColor(ContextCompat.getColor(requireContext(),
                            R.color.빨강
                        ))
                        Toast.makeText(requireContext(), "혈당 수치는 350까지 기록 가능합니다!", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = false
                    } else {
                        bloodSugarInput.setTextColor(ContextCompat.getColor(requireContext(),
                            R.color.검정색
                        ))
                        submitButton.isEnabled = true
                    }
                } catch (e: NumberFormatException) {
                    bloodSugarInput.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.검정색
                    ))
                    submitButton.isEnabled = false
                }
            }
        })


        submitButton.setOnClickListener {
            val bloodSugar = bloodSugarInput.text.toString().toInt()

            Log.d("BloodSugarInputFragment", "Sending data - date: $date, occurrenceType: $occurrenceType, bloodSugar: $bloodSugar")
            Toast.makeText(requireContext(), "혈당기록 저장됨", Toast.LENGTH_LONG).show()

            val bloodData = Blood(
                date = date,
                occurrenceType = occurrenceType,
                bloodSugar = bloodSugar
            )

            // Log the data being sent
            val gson = Gson()
            val bloodDataJson = gson.toJson(bloodData)
            Log.d("BloodSugarInputFragment", "Blood Data JSON: $bloodDataJson")

            // Get the token from SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("AuthToken", "")
            if (token.isNullOrEmpty()) {
                Log.d("BloodSugarTimeInput", "Auth token is missing.")
                return@setOnClickListener
            }

            val retrofitService = createRetrofitService() // RetrofitService 객체 생성
            val call = retrofitService.postBloodSugarData(bloodData, "Bearer $token")
            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            Log.d("BloodSugarInputFragment", "Response: ${apiResponse.message}, Success: ${apiResponse.success}")
                            if (apiResponse.success) {
                                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                    putExtra("SELECTED_DATE", date)
                                }
                                startActivity(intent)
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("BloodSugarInputFragment", "Response was not successful: Code ${response.code()}, Error Body: $errorBody")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("BloodSugarInputFragment", "Failed to post data", t)
                }
            })
        }
    }

    companion object {
        private const val BASE_URL = "http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/"

        fun createRetrofitService(): RetrofitService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(RetrofitService::class.java)
        }
    }
}
