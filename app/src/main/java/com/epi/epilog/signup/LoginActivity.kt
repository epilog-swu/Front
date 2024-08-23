package com.epi.epilog.signup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.epi.epilog.api.ApiResponse
import com.epi.epilog.R
import com.epi.epilog.api.LoginRequest
import com.epi.epilog.api.RetrofitService
import com.epi.epilog.api.TokenData
import com.epi.epilog.main.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_patient)

        initializeRetrofit()

        // 권한 요청
        checkAndRequestPermissions()

        val preButton: Button = findViewById(R.id.pre_button)
        val loginButton: Button = findViewById(R.id.login_button)
        val loginIdEditText: EditText = findViewById(R.id.id_edit)
        val passwordEditText: EditText = findViewById(R.id.password_edit)
        val signUpText: TextView = findViewById(R.id.signup_text)

        preButton.setOnClickListener {
            val intent = Intent(this, startActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val loginId = loginIdEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (loginId.isNotEmpty() && password.isNotEmpty()) {
                login(LoginRequest(loginId, password))
            } else {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
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

    private fun login(request: LoginRequest) {
        retrofitService.login(request).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    saveTokenToSession(responseBody)
                    Log.d(TAG, "Token: $responseBody")
//                    sendFCMToken(responseBody) // FCM 토큰 전송 추가
                } else {
                    Log.d(TAG, "Login failed with HTTP status code: ${response.code()}")
                    Toast.makeText(this@LoginActivity, "로그인 실패: HTTP status code ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "로그인 실패: ${t.message}")
                Toast.makeText(this@LoginActivity, "로그인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //TODO : FCM 기기 토큰 서버로 전송 -> 추후에 서버와 연동하여 음성인식 기능 도입 예정
    private fun sendFCMToken(authToken: String?) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                val tokenData = TokenData(token = fcmToken)

                retrofitService.postToken("Bearer $authToken", tokenData).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d(TAG, "FCM 토큰 전송 성공: $fcmToken")
                        } else {
                            Log.d(TAG, "FCM 토큰 전송 실패")
                        }
                        navigateToMainActivity() // FCM 토큰 전송 후 메인 액티비티로 이동
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.d(TAG, "FCM 토큰 전송 실패: ${t.message}")
                        navigateToMainActivity() // 실패해도 메인 액티비티로 이동
                    }
                })
            } else {
                Log.w(TAG, "FCM 토큰 가져오기 실패", task.exception)
                navigateToMainActivity() // FCM 토큰 가져오기 실패 시에도 메인 액티비티로 이동
            }
        }
    }
    // 권한 요청
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.BODY_SENSORS,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val perms = HashMap<String, Int>()
            perms[android.Manifest.permission.BODY_SENSORS] = PackageManager.PERMISSION_GRANTED
            perms[android.Manifest.permission.POST_NOTIFICATIONS] = PackageManager.PERMISSION_GRANTED
            perms[android.Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED
            perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED

            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    perms[permissions[i]] = grantResults[i]
                }

                if (perms[android.Manifest.permission.BODY_SENSORS] == PackageManager.PERMISSION_GRANTED
                    && perms[android.Manifest.permission.POST_NOTIFICATIONS] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "All permissions granted")
                } else {
                    Toast.makeText(this, "권한 설정을 해주세요", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun saveTokenToSession(token: String?) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
        const val PERMISSION_REQUEST_CODE = 100
    }
}
