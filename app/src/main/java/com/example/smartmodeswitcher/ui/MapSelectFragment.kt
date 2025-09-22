package com.example.smartmodeswitcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import android.preference.PreferenceManager
import com.example.smartmodeswitcher.R
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

class MapSelectFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var editSpotName: EditText
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val root = inflater.inflate(R.layout.fragment_map_select, container, false)
        mapView = root.findViewById(R.id.map)
        editSpotName = root.findViewById(R.id.editSpotName)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(35.681236, 139.767125)) // 東京駅

        // 地図タップでピン設置
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    selectedPoint = p
                    // 既存マーカー削除
                    marker?.let { mapView.overlays.remove(it) }
                    // 新しいマーカー設置
                    marker = Marker(mapView).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = editSpotName.text.toString().ifBlank { "選択スポット" }
                    }
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))

        root.findViewById<Button>(R.id.buttonSelect).setOnClickListener {
            val spotName = editSpotName.text.toString()
            val point = selectedPoint
            if (spotName.isNotBlank() && point != null) {
                // FragmentResultで呼び出し元に返す
                parentFragmentManager.setFragmentResult(
                    "map_select_result",
                    Bundle().apply {
                        putString("spot_name", spotName)
                        putDouble("latitude", point.latitude)
                        putDouble("longitude", point.longitude)
                    }
                )
                parentFragmentManager.popBackStack()
            } else {
                editSpotName.error = if (spotName.isBlank()) "スポット名を入力してください" else null
                if (point == null) mapView.controller.animateTo(GeoPoint(35.681236, 139.767125))
            }
        }

        return root
    }
}
