package com.epi.epilog.presentation

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        if (isLoggedIn()) {
            validateTokenAndNavigate()
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
                    showInvalidCodeDialog("코드를 입력하세요.")
                }
            } else {
                Toast.makeText(this, "네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        checkAndRequestPermissions()
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
                        saveTokenToSession(it)
                        setLoggedIn(true)
                        disableBatteryOptimization()
                        getTokenAndNavigate()
                    }
                } else {
                    Log.d(TAG, "Error Response: ${response.errorBody()?.string()}")
                    Log.d(TAG, "Response Code: ${response.code()}")
                    showInvalidCodeDialog("코드 검증에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "POST failed: ${t.message}")
                Toast.makeText(this@LoginActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTokenToSession(token: String?) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
    }

    private fun setLoggedIn(loggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("LoggedIn", loggedIn).apply()
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null) != null
    }

    private fun validateTokenAndNavigate() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AuthToken", null) ?: return

        retrofitService.testApi("Bearer $authToken").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    getTokenAndNavigate()
                } else {
                    saveTokenToSession(null)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Token validation failed: ${t.message}")
                saveTokenToSession(null)
            }
        })
    }

    private fun getTokenAndNavigate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            sendTokenToServer(token)
            navigateToMainActivity()
        }
    }

    private fun sendTokenToServer(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AuthToken", null) ?: return

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
            .setMessage(message)
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

    companion object {
        private const val TAG = "LoginActivity"
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
