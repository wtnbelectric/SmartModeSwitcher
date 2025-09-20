package com.example.smartmodeswitcher.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smartmodeswitcher.R
import com.example.smartmodeswitcher.databinding.FragmentRuleEditBinding
import com.example.smartmodeswitcher.data.AppDatabase
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.data.RuleRepository
import com.example.smartmodeswitcher.ui.RuleListViewModelFactory
import java.util.*

class RuleEditFragment : Fragment() {
    private var _binding: FragmentRuleEditBinding? = null
    private val binding get() = _binding!!

    // 時刻保持用
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0
    private var selectedMode = 1 // 1:通常, 2:バイブ, 3:サイレント

    private lateinit var ruleListViewModel: RuleListViewModel

    // 編集中のルールID
    private var editingRuleId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRuleEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModelの初期化（Factoryを使う）
        val db = AppDatabase.getInstance(requireContext().applicationContext)
        val repository = RuleRepository(db.ruleDao())
        val factory = RuleListViewModelFactory(repository)
        ruleListViewModel = ViewModelProvider(this, factory)[RuleListViewModel::class.java]

        // 編集モード判定
        editingRuleId = arguments?.getInt("rule_id")
        if (editingRuleId != null) {
            // 既存ルールを取得してUIに反映
            ruleListViewModel.allRules.observe(viewLifecycleOwner) { rules ->
                val rule = rules.find { it.id == editingRuleId }
                if (rule != null) {
                    startHour = rule.startTime.substringBefore(":").toInt()
                    startMinute = rule.startTime.substringAfter(":").toInt()
                    endHour = rule.endTime.substringBefore(":").toInt()
                    endMinute = rule.endTime.substringAfter(":").toInt()
                    binding.buttonStartTime.text = rule.startTime
                    binding.buttonEndTime.text = rule.endTime
                    selectedMode = rule.mode
                    when (rule.mode) {
                        1 -> binding.radioButtonNormal.isChecked = true
                        2 -> binding.radioButtonVibrate.isChecked = true
                        3 -> binding.radioButtonSilent.isChecked = true
                    }
                    // 曜日
                    if (rule.days.length == 7) {
                        binding.checkSun.isChecked = rule.days[0] == '1'
                        binding.checkMon.isChecked = rule.days[1] == '1'
                        binding.checkTue.isChecked = rule.days[2] == '1'
                        binding.checkWed.isChecked = rule.days[3] == '1'
                        binding.checkThu.isChecked = rule.days[4] == '1'
                        binding.checkFri.isChecked = rule.days[5] == '1'
                        binding.checkSat.isChecked = rule.days[6] == '1'
                    }
                    // 位置情報
                    binding.editLatitude.setText(rule.latitude?.toString())
                    binding.editLongitude.setText(rule.longitude?.toString())
                    binding.editRadius.setText(rule.radius?.toString())
                }
            }
        }

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

        // 保存ボタン押下時
        binding.buttonSave.setOnClickListener {
            // 曜日チェックボックスの状態を"0111110"形式で取得
            val days = buildString {
                append(if (binding.checkSun.isChecked) "1" else "0")
                append(if (binding.checkMon.isChecked) "1" else "0")
                append(if (binding.checkTue.isChecked) "1" else "0")
                append(if (binding.checkWed.isChecked) "1" else "0")
                append(if (binding.checkThu.isChecked) "1" else "0")
                append(if (binding.checkFri.isChecked) "1" else "0")
                append(if (binding.checkSat.isChecked) "1" else "0")
            }

            // 位置情報入力値の取得
            val latitudeText = binding.editLatitude.text.toString().trim()
            val longitudeText = binding.editLongitude.text.toString().trim()
            val radiusText = binding.editRadius.text.toString().trim()

            val latitude = latitudeText.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val longitude = longitudeText.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val radius = radiusText.takeIf { it.isNotEmpty() }?.toIntOrNull()
            val dayOfWeek = days.indexOf('1') // 例: "0100000"なら1（＝月曜）

            val rule = Rule(
                id = editingRuleId ?: 0,
                startTime = String.format("%02d:%02d", startHour, startMinute),
                endTime = String.format("%02d:%02d", endHour, endMinute),
                days = days,
                mode = selectedMode,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                dayOfWeek = dayOfWeek
            )
            if (editingRuleId != null) {
                ruleListViewModel.update(rule)
                Toast.makeText(requireContext(), "ルールを更新しました", Toast.LENGTH_SHORT).show()
            } else {
                ruleListViewModel.insert(rule)
                Toast.makeText(requireContext(), "ルールを保存しました", Toast.LENGTH_SHORT).show()
            }
            // ルール一覧画面に明示的に遷移
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.smartmodeswitcher.ui.RuleListFragment())
                .commit()
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