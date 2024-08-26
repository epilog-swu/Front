package com.epi.epilog.signup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.epi.epilog.R
import com.epi.epilog.api.LoginRequest
import com.epi.epilog.api.RetrofitService
import com.epi.epilog.main.MainActivity
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService
    private lateinit var passwordEditText: EditText
    private lateinit var togglePasswordVisibility: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_patient)

        // 앱 시작 시 권한 요청
        checkAndRequestPermissions()

        initializeRetrofit()

        val preButton: Button = findViewById(R.id.pre_button)
        val loginButton: Button = findViewById(R.id.login_button)
        val loginIdEditText: EditText = findViewById(R.id.id_edit)
        passwordEditText = findViewById(R.id.password_edit)
        val signUpText: TextView = findViewById(R.id.signup_text)

        // 회원가입 텍스트에 밑줄 추가 및 클릭 이벤트 추가
        val signUpFullText = "계정이 없으신가요? 회원가입하기"
        val spannableString = SpannableString(signUpFullText)
        val startIndex = signUpFullText.indexOf("회원가입하기")
        val endIndex = startIndex + "회원가입하기".length

        spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, signUp1Activity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpText.text = spannableString
        signUpText.movementMethod = LinkMovementMethod.getInstance()

        // 기본적으로 비밀번호 숨김 설정
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // 비밀번호 보이기/숨기기 기능 추가
        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[drawableRight].bounds.width())) {
                    togglePasswordVisibility(passwordEditText)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // 이전 버튼
        preButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼
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

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
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
                    navigateToMainActivity()
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

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // 센서 권한 확인
        val sensorPermission = android.Manifest.permission.BODY_SENSORS
        if (ContextCompat.checkSelfPermission(this, sensorPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(sensorPermission)
        }

        // 알림 권한 확인
        val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(notificationPermission)
        }

        // 위치 권한 확인 (둘 중 하나만 허용되면 됨)
        val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(fineLocationPermission)
            permissionsNeeded.add(coarseLocationPermission)
        }

        if (permissionsNeeded.isNotEmpty()) {
            // 필요한 모든 권한을 한 번에 요청
            Toast.makeText(this@LoginActivity, "앱 실행을 위해 모든 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionsToRequestAgain = mutableListOf<String>()

            // 센서 권한이 거부되었는지 확인
            val sensorPermission = android.Manifest.permission.BODY_SENSORS
            if (ContextCompat.checkSelfPermission(this, sensorPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequestAgain.add(sensorPermission)
            }

            // 알림 권한이 거부되었는지 확인
            val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequestAgain.add(notificationPermission)
            }

            // 위치 권한이 둘 다 거부되었는지 확인 (둘 중 하나라도 허용된 경우 무시)
            val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
            val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
            if (ContextCompat.checkSelfPermission(this, fineLocationPermission) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequestAgain.add(fineLocationPermission)
                permissionsToRequestAgain.add(coarseLocationPermission)
            }

            if (permissionsToRequestAgain.isNotEmpty()) {
                // 아직 허용되지 않은 권한이 있다면 다시 요청
                ActivityCompat.requestPermissions(this, permissionsToRequestAgain.toTypedArray(), PERMISSION_REQUEST_CODE)
            } else {
                // 모든 권한이 허용된 상태라면 초기화 진행
            }

            // 권한 요청 후에도 필요한 권한이 허용되지 않았다면 설정 페이지로 이동
            if (permissionsToRequestAgain.any {
                    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                }) {
                redirectToSettings()
            }
        }
    }

    private fun redirectToSettings() {
        Toast.makeText(this, "설정에서 모든 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
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
