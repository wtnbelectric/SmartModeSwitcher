package com.example.smartmodeswitcher.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartmodeswitcher.databinding.FragmentRuleEditBinding

class RuleEditFragment : Fragment() {
    private var _binding: FragmentRuleEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRuleEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStartTime.setOnClickListener {
            // TimePickerDialogを表示
            // 入力値を保存
            Toast.makeText(requireContext(), "ルールを保存しました", Toast.LENGTH_SHORT).show()
            // 必要な場所で
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }

        binding.buttonCancel.setOnClickListener {
            // キャンセル処理
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}