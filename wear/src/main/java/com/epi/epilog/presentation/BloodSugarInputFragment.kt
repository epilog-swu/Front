package com.epi.epilog.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import androidx.fragment.app.Fragment
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
        submitButton.setOnClickListener {
            val bloodSugar = bloodSugarInput.text.toString().toInt()

            Log.d("BloodSugarInputFragment", "Sending data - date: $date, occurrenceType: $occurrenceType, bloodSugar: $bloodSugar")

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
                                val intent = Intent(requireActivity(), MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
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
