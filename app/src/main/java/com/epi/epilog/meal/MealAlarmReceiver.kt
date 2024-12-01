package com.epi.epilog.meal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MealAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val mealType = intent?.getStringExtra("mealType") ?: "식사"
        val time = intent?.getStringExtra("time") ?: "시간"

        // 알림(Notification)을 통해 사용자에게 알람 전달
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "meal_alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Meal Alarm", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("식사 알람")
            .setContentText("$mealType 시간입니다! ($time)")
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // 기본 아이콘 사용
            .build()
        notificationManager.notify(0, notification)
    }
}
