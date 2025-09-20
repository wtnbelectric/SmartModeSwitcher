package com.example.smartmodeswitcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var selectedDate: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // シンプルなレイアウトをコードで生成
        val root = inflater.inflate(
            com.example.smartmodeswitcher.R.layout.fragment_dashboard, container, false
        )
        val buttonPickDate = root.findViewById<Button>(com.example.smartmodeswitcher.R.id.buttonPickDate)
        val textSelectedDate = root.findViewById<TextView>(com.example.smartmodeswitcher.R.id.textSelectedDate)

        // 初期表示
        textSelectedDate.text = formatDate(selectedDate)

        buttonPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("日付を選択")
                .setSelection(selectedDate)
                .build()
            picker.addOnPositiveButtonClickListener { dateMillis ->
                selectedDate = dateMillis
                textSelectedDate.text = formatDate(selectedDate)
                // ここで選択日付に応じたルール表示処理を呼び出す
            }
            picker.show(parentFragmentManager, "date_picker")
        }

        return root
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
