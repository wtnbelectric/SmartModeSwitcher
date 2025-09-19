package com.example.smartmodeswitcher.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartmodeswitcher.R
import com.example.smartmodeswitcher.databinding.FragmentRuleEditBinding
import java.util.*
import com.example.smartmodeswitcher.data.Rule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RuleEditFragment : Fragment() {
    private var _binding: FragmentRuleEditBinding? = null
    private val binding get() = _binding!!

    // 時刻保持用
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0
    private var selectedMode = 1 // 1:通常, 2:バイブ, 3:サイレント

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRuleEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 開始時間ボタン
        binding.buttonStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                startHour = hour
                startMinute = minute
                binding.buttonStartTime.text = String.format("%02d:%02d", hour, minute)
            }, startHour, startMinute, true).show()
        }

        // 終了時間ボタン
        binding.buttonEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                endHour = hour
                endMinute = minute
                binding.buttonEndTime.text = String.format("%02d:%02d", hour, minute)
            }, endHour, endMinute, true).show()
        }

        // モード選択（ラジオボタン想定）
        binding.radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            selectedMode = when (checkedId) {
                binding.radioButtonNormal.id -> 1
                binding.radioButtonVibrate.id -> 2
                binding.radioButtonSilent.id -> 3
                else -> 1
            }
        }

        // 保存ボタン
        binding.buttonSave.setOnClickListener {
            val rule = Rule(
                startTime = String.format("%02d:%02d", startHour, startMinute),
                endTime = String.format("%02d:%02d", endHour, endMinute),
                mode = selectedMode
            )
            Toast.makeText(requireContext(), "ルールを保存しました", Toast.LENGTH_SHORT).show()
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // キャンセルボタン
        binding.buttonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}