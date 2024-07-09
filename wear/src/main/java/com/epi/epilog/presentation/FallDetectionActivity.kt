package com.epi.epilog.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
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
import java.util.Timer
import java.util.TimerTask

class FallDetectionActivity : ComponentActivity() {

    private val timer = Timer()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound)
        initializeRetrofit()
        playSound()
        findViewById<Button>(R.id.dialog_fall_button_yes).setOnClickListener {
//            sendLocationData()
            navigateToMainActivity()
        }

        findViewById<Button>(R.id.dialog_fall_button_no).setOnClickListener {
            navigateToMainActivity()
        }
        //10초동안 버튼 클릭 X -> 위치 전송
        handler.postDelayed({
//            sendLocationData()
            navigateToMainActivity()
        }, 10000)
    }

    private fun playSound() {
        mediaPlayer.start()
        handler.postDelayed({
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)

        }, 3500)
    }

    private fun sendLocationData() {

        // Hardcoded latitude and longitude
        val lat = 37.6292514
        val lon = 127.0904845
        val locationData = LocationData(lat, lon)
        Log.d("좌표", "Latitude: $lat, Longitude: $lon")  // 현재 위도와 경도 값을 출력

        postLocationData(locationData)


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

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
