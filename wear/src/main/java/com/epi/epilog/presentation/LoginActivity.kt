package com.epi.epilog.presentation

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.Data
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.TokenData
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : ComponentActivity() {

    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeRetrofit()

        if (isLoggedIn()) { //로그인한 상태일 경우
            getTokenAndNavigate()
            return
        }

        setContentView(R.layout.login)

        val codeInput = findViewById<EditText>(R.id.editTextText)
        val loginButton = findViewById<Button>(R.id.button)

        loginButton.setOnClickListener {
            val code = codeInput.text.toString()
            if (code.isNotEmpty() && code == "123456") {
                postData(code)
            } else {
                showInvalidCodeDialog() //다른 연동코드 입력 시
            }
        }
    }

    private fun initializeRetrofit() {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun postData(code: String) {
        val post = Data(code = code)
        val call = retrofitService.postData(post)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("LoginActivity", "Server Response: $it")
                        saveTokenToSession(it) //토큰 저장
                        setLoggedIn(true)
                        getTokenAndNavigate() //FCM 토큰 가져와서 출력 및 메인 액티비티로 이동
                        disableBatteryOptimization()
                    }
                } else {
                    Log.d("LoginActivity", "Error Response: ${response.errorBody()?.string()}")
                    Log.d("LoginActivity", "Response Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("LoginActivity", "POST failed: ${t.message}")
            }
        })
    }

    private fun saveTokenToSession(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
    }

    private fun setLoggedIn(loggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("LoggedIn", loggedIn).apply()
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("LoggedIn", false)
    }

    private fun getTokenAndNavigate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // FCM 토큰
            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            // FCM 토큰 서버로 보내기
            sendTokenToServer(token)

            navigateToMainActivity()
            finish()
        }
    }

    private fun sendTokenToServer(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AuthToken", null)

        if (authToken.isNullOrEmpty()) {
            Log.d("FCM", "Auth token is missing")
            return
        }

        val tokenData = TokenData(token = token)
        val call = retrofitService.postToken("Bearer $authToken", tokenData)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse?.success == true) {
                        Log.d("FCM", "Token saved successfully on the server: ${tokenResponse.message}")
                    } else {
                        Log.d("FCM", "Failed to save token on the server: ${tokenResponse?.message}")
                    }
                } else {
                    Log.d("FCM", "Error saving token on the server: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("LoginActivity", "Failed to send token to server: ${t.message}")
            }
        })
    }



    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun disableBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun showInvalidCodeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.login_modal, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
