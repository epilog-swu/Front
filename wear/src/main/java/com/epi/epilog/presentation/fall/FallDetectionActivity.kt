package com.epi.epilog.presentation.fall

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.MainActivity
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.api.LocationData
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FallDetectionActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var retrofitService: RetrofitService
    private lateinit var timerTextView: TextView
    private var timeRemaining = 15 // 15초 타이머
    private var timerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound)
        timerTextView = findViewById(R.id.timer_text)

        initializeRetrofit()
        playSound()

        findViewById<Button>(R.id.dialog_fall_button_yes).setOnClickListener {
            cancelTimer()
            sendFallDetectionResult(true) {
                navigateToMainActivity()
            }
        }

        findViewById<Button>(R.id.dialog_fall_button_no).setOnClickListener {
            cancelTimer()
            sendFallDetectionResult(false) {
                navigateToMainActivity()
            }
        }

        startTimer()
    }

    private fun playSound() {
        mediaPlayer.start()
        handler.postDelayed({
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }, 15000)
    }

    private fun sendFallDetectionResult(isFallConfirmed: Boolean, onComplete: () -> Unit) {
        val intent = Intent("com.epi.epilog.FALL_DETECTION_RESULT")
        intent.putExtra("isFallConfirmed", isFallConfirmed)
        sendBroadcast(intent)

        // "비상 연락 전송됨" 토스트 메시지 띄우기
        if (isFallConfirmed) {
            Toast.makeText(this, "비상 연락 전송됨", Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(onComplete, 2000)  // 일정 시간 후에 onComplete 호출
    }

    private fun initializeRetrofit() {
        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun postLocationData(locationData: LocationData) {
        val token = getTokenFromSession()

        if (!token.isNullOrEmpty()) {
            retrofitService.postLocationData(locationData, "Bearer $token").enqueue(object :
                Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.success) {
                            Log.d("Location", "Location sent successfully")
                        } else {
                            Log.e("Location", "Failed to send location: ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.e("Location", "Failed to send location: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("Location", "Failed to send location: ${t.message}")
                }
            })
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (timeRemaining > 0) {
                    timerTextView.text = timeRemaining.toString()
                    timeRemaining--
                    handler.postDelayed(this, 1000)
                } else {
                    sendFallDetectionResult(true) {
                        navigateToMainActivity()
                    }
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun cancelTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
        mediaPlayer.release()
    }
}
