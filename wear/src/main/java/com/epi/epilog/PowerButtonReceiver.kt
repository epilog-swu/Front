package com.epi.epilog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class PowerButtonReceiver : BroadcastReceiver() {
    private var powerButtonPressCount = 0
    private var handler = Handler(Looper.getMainLooper())
    private val resetPressCountRunnable = Runnable { powerButtonPressCount = 0 }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF || intent.action == Intent.ACTION_SCREEN_ON) {
            powerButtonPressCount++
            handler.removeCallbacks(resetPressCountRunnable)
            handler.postDelayed(resetPressCountRunnable, 2000)

            if (powerButtonPressCount == 3) {
                Log.d("PowerButtonReceiver", "Triple press detected.")
                playFallASound(context)
                powerButtonPressCount = 0
            }
        }
    }

    private fun playFallASound(context: Context) {
        val fallASound = MediaPlayer.create(context, R.raw.fall_a)
        fallASound.start()
    }
}
