package com.example.smartmodeswitcher.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class ModeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mode = intent.getIntExtra("mode", 1)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (mode) {
            1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            2 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            3 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }
    }
}