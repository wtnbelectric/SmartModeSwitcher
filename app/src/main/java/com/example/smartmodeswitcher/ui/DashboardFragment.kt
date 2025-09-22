package com.example.smartmodeswitcher.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmodeswitcher.R
import com.example.smartmodeswitcher.data.AppDatabase
import com.example.smartmodeswitcher.data.RuleRepository
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import java.time.Instant
import java.time.ZoneId
import com.example.smartmodeswitcher.ui.DashboardViewModel
import com.example.smartmodeswitcher.ui.RuleAdapter
import java.time.LocalDate
import android.widget.FrameLayout
import android.util.TypedValue
import android.graphics.Color
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

import com.example.smartmodeswitcher.receiver.GeofenceBroadcastReceiver
import androidx.core.content.ContextCompat

class DashboardFragment : Fragment() {
    private var selectedDate: Long = MaterialDatePicker.todayInUtcMilliseconds()

    // DB, Repository, Factoryの初期化
    private val db by lazy { AppDatabase.getInstance(requireContext().applicationContext) }
    private val repository by lazy { RuleRepository(db.ruleDao()) }
    private val factory by lazy { DashboardViewModelFactory(repository) }
    private val viewModel: DashboardViewModel by viewModels { factory }

    private lateinit var ganttChartView: GanttChartView
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val buttonPickDate = root.findViewById<Button>(R.id.buttonPickDate)
        val textSelectedDate = root.findViewById<TextView>(R.id.textSelectedDate)

        // RecyclerViewとアダプタのセット
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerViewRules)
        val adapter = RuleAdapter { rule, isEnabled ->
            // 有効/無効変更時にDBへ反映
            val updatedRule = rule.copy(enabled = isEnabled)
            viewModel.updateRule(updatedRule)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // ガントチャートViewのセットアップ
        val ganttContainer = root.findViewById<FrameLayout>(R.id.ganttChartContainer)
        ganttChartView = GanttChartView(requireContext())
        ganttContainer.addView(ganttChartView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // LiveData監視
        viewModel.rules.observe(viewLifecycleOwner) { rules ->
            adapter.submitList(rules)
            ganttChartView.setRules(rules)
            // Geofenceリストの作成
            val geofenceList = createGeofenceList(rules)
            android.util.Log.d("DashboardFragment", "Geofenceリスト作成: ${geofenceList.size}件")

            // Geofence登録
            if (geofenceList.isNotEmpty()) {
                val geofencingRequest = createGeofencingRequest(geofenceList)
                val pendingIntent = getGeofencePendingIntent()
                // パーミッションチェック
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                        .addOnSuccessListener {
                            android.util.Log.d("DashboardFragment", "Geofence登録成功")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("DashboardFragment", "Geofence登録失敗: ${e.message}")
                        }
                } else {
                    android.util.Log.e("DashboardFragment", "Geofence登録失敗: 位置情報権限なし")
                }
            }
        }

        // 現在有効なルールIDの監視（ハイライト用）
        viewModel.currentActiveRuleId.observe(viewLifecycleOwner) { activeId ->
            // ここでUIに反映
            adapter.setActiveRuleId(activeId)
            ganttChartView.setActiveRuleId(activeId)
        }

        // 初期表示時に現在の日付でルールを読み込む
        val currentDate = LocalDate.now()
        textSelectedDate.text = formatDate(selectedDate)
        viewModel.loadRulesForDate(currentDate)

        // 2. 日付選択ボタンのリスナー
        buttonPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("日付を選択")
                .setSelection(selectedDate)
                .build()
            picker.addOnPositiveButtonClickListener { dateMillis ->
                selectedDate = dateMillis
                textSelectedDate.text = formatDate(selectedDate)
                // 3. 日付選択後のViewModel呼び出し
                val localDate = Instant.ofEpochMilli(selectedDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                viewModel.loadRulesForDate(localDate)
            }
            picker.show(parentFragmentManager, "date_picker")
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return root
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    /**
     * MainActivityから呼ばれる: ジオフェンスや位置情報イベントで有効になったルールIDを受け取りUIに反映
     */
    fun handleRuleSearchResult(ruleId: Long?) {
        android.util.Log.d("DashboardFragment", "handleRuleSearchResult: ruleId=$ruleId")
        viewModel.setCurrentActiveRuleId(ruleId)
    }

    /**
     * 位置情報付きルールからGeofenceリストを作成
     */
    private fun createGeofenceList(rules: List<com.example.smartmodeswitcher.data.Rule>): List<Geofence> {
        return rules.filter { it.latitude != null && it.longitude != null && it.radius != null }
            .map { rule ->
                Geofence.Builder()
                    .setRequestId("rule_${rule.id}")
                    .setCircularRegion(
                        rule.latitude!!,
                        rule.longitude!!,
                        rule.radius!!.toFloat()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                    )
                    .build()
            }
    }

    /**
     * GeofenceリストからGeofencingRequestを作成
     */
    private fun createGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .apply {
                geofenceList.forEach { addGeofence(it) }
            }
            .build()
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}