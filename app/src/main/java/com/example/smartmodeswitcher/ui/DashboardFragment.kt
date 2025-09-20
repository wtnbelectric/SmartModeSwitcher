package com.example.smartmodeswitcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ViewModelのimport（別途作成が必要）
import com.example.smartmodeswitcher.ui.DashboardViewModel

import com.example.smartmodeswitcher.data.AppDatabase
import com.example.smartmodeswitcher.data.RuleRepository

class DashboardFragment : Fragment() {
    private var selectedDate: Long = MaterialDatePicker.todayInUtcMilliseconds()

    // DB, Repository, Factoryの初期化
    private val db by lazy { AppDatabase.getInstance(requireContext().applicationContext) }
    private val repository by lazy { RuleRepository(db.ruleDao()) }
    private val factory by lazy { DashboardViewModelFactory(repository) }
    private val viewModel: DashboardViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(
            com.example.smartmodeswitcher.R.layout.fragment_dashboard, container, false
        )
        val buttonPickDate = root.findViewById<Button>(com.example.smartmodeswitcher.R.id.buttonPickDate)
        val textSelectedDate = root.findViewById<TextView>(com.example.smartmodeswitcher.R.id.textSelectedDate)

        textSelectedDate.text = formatDate(selectedDate)

        buttonPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("日付を選択")
                .setSelection(selectedDate)
                .build()
            picker.addOnPositiveButtonClickListener { dateMillis ->
                selectedDate = dateMillis
                textSelectedDate.text = formatDate(selectedDate)
                // ミリ秒からLocalDateを生成してViewModelに渡す
                val localDate = Instant.ofEpochMilli(selectedDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                viewModel.loadRulesForDate(localDate)
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