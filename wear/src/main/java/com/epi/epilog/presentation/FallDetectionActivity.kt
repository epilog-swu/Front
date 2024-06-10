package com.epi.epilog.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.epi.epilog.FallDetectionService
import com.epi.epilog.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Timer
import java.util.TimerTask

class FallDetectionActivity : ComponentActivity() {

    private val timer = Timer()
    private val locationRequestCode = 1000
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mediaPlayer: MediaPlayer // MediaPlayer 추가

    companion object {
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection)

        acquireWakeLock()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.dialog_fall_button_no).setOnClickListener {
            restartFallDetectionService()
        }

        findViewById<Button>(R.id.dialog_fall_button_yes).setOnClickListener {
            requestLocationPermissionAndLogCoordinates()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound) // 긴급상황 사운드 재생
        mediaPlayer.start()

        timer.schedule(object : TimerTask() {
            override fun run() {
                logCoordinates()
                restartFallDetectionService()
            }
        }, 15000)
    }

    private fun restartFallDetectionService() {
        val intent = Intent(this, FallDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish()
    }

    private fun requestLocationPermissionAndLogCoordinates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, locationPermissions, locationRequestCode)
        } else {
            getLastKnownLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastKnownLocation = location
                    Log.d("FallDetectionActivity", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    Toast.makeText(this, "Latitude: ${location.latitude}, Longitude: ${location.longitude}", Toast.LENGTH_LONG).show()
                } else {
                    Log.d("FallDetectionActivity", "No location found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FallDetectionActivity", "Failed to get location: ${e.message}")
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

    private fun logCoordinates() {
        lastKnownLocation?.let {
            Log.d("FallDetectionActivity", "Logging Coordinates - Latitude: ${it.latitude}, Longitude: ${it.longitude}")
        } ?: Log.d("FallDetectionActivity", "No known location to log.")
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        mediaPlayer.release() // MediaPlayer 해제
    }
}
