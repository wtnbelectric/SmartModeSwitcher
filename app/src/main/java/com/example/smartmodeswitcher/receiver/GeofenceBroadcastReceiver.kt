package com.example.smartmodeswitcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }
        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Geofencing error: ${event.errorCode}")
            return
        }
        val transition = event.geofenceTransition
        val transitionStr = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            else -> "OTHER"
        }
        val triggeredIds = event.triggeringGeofences?.map { it.requestId } ?: emptyList()
        Log.d("GeofenceReceiver", "Geofence event: $transitionStr, ids=$triggeredIds")

        // --- ここからアプリ本体へ伝搬 ---
        val broadcastIntent = Intent("com.example.smartmodeswitcher.GEOFENCE_EVENT")
        broadcastIntent.putExtra("transition", transitionStr)
        broadcastIntent.putStringArrayListExtra("rule_ids", ArrayList(triggeredIds))
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }
}