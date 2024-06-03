package com.epi.epilog.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

class FallDetectionActivity : ComponentActivity() {

    private val timer = Timer()
    private val locationRequestCode = 1000
    private lateinit var locationManager: LocationManager
    private lateinit var myLocationListener: MyLocationListener
    private var isDialogShowing = false
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var retrofitService: RetrofitService
    private var lastKnownLocation: Location? = null

    companion object {
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection)

        Log.d("FallDetectionActivity", "onCreate called")

        acquireWakeLock()

        findViewById<Button>(R.id.dialog_fall_button_no).setOnClickListener {
            navigateToMainActivity()
        }

        findViewById<Button>(R.id.dialog_fall_button_yes).setOnClickListener {
            requestLocationPermissionAndPostEmergencyContact()
        }

        timer.run {
            schedule(object : TimerTask() {
                override fun run() {
                    sendEmergencyContactData()
                    navigateToMainActivity()
                }
            }, 15000)
        }

        // Initialize the LocationManager here
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        initializeRetrofit()
    }

    private fun initializeRetrofit() {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun requestLocationPermissionAndPostEmergencyContact() {
        Log.d("FallDetectionActivity", "requestLocationPermissionAndPostEmergencyContact called")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, locationPermissions, locationRequestCode)
        } else {
            setLocationListener()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                setLocationListener()
            } else {
                Log.d("FallDetectionActivity", "Location permission denied.")
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("MissingPermission")
    private fun setLocationListener() {
        if (::locationManager.isInitialized.not()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        val minTime: Long = 1500
        val minDistance = 100f

        if (::myLocationListener.isInitialized.not()) {
            myLocationListener = MyLocationListener()
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime, minDistance, myLocationListener
            )
        } else {
            Toast.makeText(this, "GPS is not enabled. Please enable GPS.", Toast.LENGTH_LONG).show()
        }
    }

    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("FallDetectionActivity", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
            Toast.makeText(this@FallDetectionActivity, "Latitude: ${location.latitude}, Longitude: ${location.longitude}", Toast.LENGTH_LONG).show()
            lastKnownLocation = location
            // 위치 정보 출력 후, 위치 업데이트를 중지합니다.
            removeLocationListener()
        }

        private fun removeLocationListener() {
            if (::locationManager.isInitialized && ::myLocationListener.isInitialized) {
                locationManager.removeUpdates(myLocationListener)
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FallDetectionActivity::WakeLock")
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun sendEmergencyContactData() {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("FallDetectionActivity", "Auth token is missing.")
            return
        }

        val data = lastKnownLocation?.let {
            mapOf("lat" to it.latitude, "long" to it.longitude)
        } ?: emptyMap()

        val call = retrofitService.postEmergencyContact(data, "Bearer $token")
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("FallDetectionActivity", "Emergency contact posted: ${response.body()}")
                } else {
                    Log.e("FallDetectionActivity", "Error posting emergency contact: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("FallDetectionActivity", "Failed to post emergency contact: ${t.message}")
            }
        })
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        if (::locationManager.isInitialized && ::myLocationListener.isInitialized) {
            locationManager.removeUpdates(myLocationListener)
        }
    }
}
