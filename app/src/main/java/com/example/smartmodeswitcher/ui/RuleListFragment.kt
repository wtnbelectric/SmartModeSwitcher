package com.example.smartmodeswitcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmodeswitcher.data.AppDatabase
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.data.RuleRepository
import com.example.smartmodeswitcher.databinding.FragmentRuleListBinding

class RuleListFragment : Fragment(), RuleListAdapter.OnRuleActionListener {
    private var _binding: FragmentRuleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RuleRepository
    private val viewModel: RuleListViewModel by viewModels {
        RuleListViewModelFactory(repository)
    }
    
    private lateinit var adapter: RuleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // RoomDatabaseとRepositoryの初期化
        val db = AppDatabase.getInstance(requireContext().applicationContext)
        repository = RuleRepository(db.ruleDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRuleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // repositoryの初期化はDIやApplicationクラス経由で行うのが推奨
        
        // Set up RecyclerView
        val adapter = RuleAdapter { rule, isEnabled ->
            // 有効/無効変更時にDBへ反映
            val updatedRule = rule.copy(enabled = isEnabled)
            viewModel.updateRule(updatedRule)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allRules.observe(viewLifecycleOwner) { rules ->
            adapter.submitList(rules)
        }

        binding.fabAdd.setOnClickListener {
            // ルール追加画面へ遷移
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.example.smartmodeswitcher.R.id.fragment_container, RuleEditFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.buttonDashboard.setOnClickListener {
            // DashboardFragmentが未実装の場合はコメントアウトしておいてください
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.example.smartmodeswitcher.R.id.fragment_container, DashboardFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onEditRule(rule: Rule) {
        // ルール編集画面へ遷移（編集対象のRuleをBundleで渡す）
        val fragment = RuleEditFragment().apply {
            arguments = Bundle().apply {
                putInt("rule_id", rule.id)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.smartmodeswitcher.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onDeleteRule(rule: Rule) {
        viewModel.delete(rule)
    }
}