package com.example.smartmodeswitcher.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartmodeswitcher.data.AppDatabase
import com.example.smartmodeswitcher.data.RuleRepository
import com.example.smartmodeswitcher.databinding.FragmentRuleListBinding

class RuleListFragment : Fragment() {
    private var _binding: FragmentRuleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RuleRepository
    private val viewModel: RuleListViewModel by viewModels {
        RuleListViewModelFactory(repository)
    }

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

        val adapter = RuleListAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.allRules.observe(viewLifecycleOwner) { rules ->
            adapter.submitList(rules)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}