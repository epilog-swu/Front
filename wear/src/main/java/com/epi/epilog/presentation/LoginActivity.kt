package com.epi.epilog.presentation

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

        if (isLoggedIn()) { // 로그인한 상태일 경우
            getTokenAndNavigate()
            return
        }

        setContentView(R.layout.login)

        val codeInput = findViewById<EditText>(R.id.editTextText)
        val loginButton = findViewById<Button>(R.id.button)

        loginButton.setOnClickListener {
            if (isNetworkAvailable()) {
                val code = codeInput.text.toString()
                if (code.isNotEmpty()) {
                    postData(code)
                } else {
                    showInvalidCodeDialog("코드를 입력하세요.") // 코드 미입력 시
                }
            } else {
                Toast.makeText(this, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
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
                        Log.d(TAG, "Server Response: $it")
                        saveTokenToSession(it) // 토큰 저장
                        setLoggedIn(true)
                        disableBatteryOptimization()
                        getTokenAndNavigate() // FCM 토큰 가져와서 출력 및 메인 액티비티로 이동
                    }
                } else {
                    Log.d(TAG, "Error Response: ${response.errorBody()?.string()}")
                    Log.d(TAG, "Response Code: ${response.code()}")
                    showInvalidCodeDialog("코드 검증에 실패했습니다.") // 서버 오류 시
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "POST failed: ${t.message}")
                Toast.makeText(this@LoginActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
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
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // FCM 토큰
            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // FCM 토큰 서버로 보내기
            sendTokenToServer(token)

            navigateToMainActivity()
        }
    }

    private fun sendTokenToServer(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AuthToken", null)

        if (authToken.isNullOrEmpty()) {
            Log.d(TAG, "Auth token is missing")
            return
        }

        val tokenData = TokenData(token = token)
        val call = retrofitService.postToken("Bearer $authToken", tokenData)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse?.success == true) {
                        Log.d(TAG, "Token saved successfully on the server: ${tokenResponse.message}")
                    } else {
                        Log.d(TAG, "Failed to save token on the server: ${tokenResponse?.message}")
                    }
                } else {
                    Log.d(TAG, "Error saving token on the server: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d(TAG, "Failed to send token to server: ${t.message}")
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

    private fun showInvalidCodeDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.login_modal, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setMessage(message) // 메시지 설정
            .create()

        dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
