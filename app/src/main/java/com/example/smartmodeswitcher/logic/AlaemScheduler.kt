// AlarmScheduler.kt
package com.example.smartmodeswitcher.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object AlarmScheduler {
    fun setRuleAlarms(context: Context, ruleId: Int, startTime: String, endTime: String, mode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 開始時刻用
        val startIntent = Intent(context, ModeChangeReceiver::class.java).apply {
            putExtra("mode", mode)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context, ruleId * 2, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val startCal = Calendar.getInstance().apply {
            val (h, m) = startTime.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, startCal.timeInMillis, startPendingIntent
        )

        // 終了時刻用（通常モードに戻す）
        val endIntent = Intent(context, ModeChangeReceiver::class.java).apply {
            putExtra("mode", 1)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context, ruleId * 2 + 1, endIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val endCal = Calendar.getInstance().apply {
            val (h, m) = endTime.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, endCal.timeInMillis, endPendingIntent
        )
    }
}