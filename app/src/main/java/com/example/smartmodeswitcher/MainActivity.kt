package com.example.smartmodeswitcher

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.smartmodeswitcher.ui.RuleListFragment
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_LOCATION = 1001
    private val REQUEST_CODE_LOCATION_BG = 1002

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var geofencingClient: GeofencingClient

    private val geofenceEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.smartmodeswitcher.GEOFENCE_EVENT") {
                val transition = intent.getStringExtra("transition")
                val ruleIds = intent.getStringArrayListExtra("rule_ids")
                // ここでは最初のIDのみをonRuleSearchResultに渡す例
                val firstRuleId = ruleIds?.firstOrNull()?.removePrefix("rule_")?.toLongOrNull()
                onRuleSearchResult(firstRuleId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RuleListFragment())
                .commit()
        }
        geofencingClient = LocationServices.getGeofencingClient(this)
        checkAndRequestLocationPermissions()
    }

    private fun checkAndRequestLocationPermissions() {
        val missing = locationPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), REQUEST_CODE_LOCATION)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ の場合、バックグラウンドも追加でリクエスト
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_LOCATION_BG)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 権限が拒否された場合は説明ダイアログを表示
        if (grantResults.isNotEmpty() && grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            AlertDialog.Builder(this)
                .setTitle("権限が必要です")
                .setMessage("このアプリの機能を利用するには位置情報の権限が必要です。設定から権限を許可してください。")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            geofenceEventReceiver,
            IntentFilter("com.example.smartmodeswitcher.GEOFENCE_EVENT")
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geofenceEventReceiver)
    }

    // ジオフェンスや位置情報イベントを受け取った場合のハンドリング例
    fun onRuleSearchResult(ruleId: Long?) {
        // Fragmentに伝搬する例
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is com.example.smartmodeswitcher.ui.DashboardFragment) {
            fragment.handleRuleSearchResult(ruleId)
        }
    }
}