package com.example.smartmodeswitcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.data.RuleRepository

class RuleListViewModel(private val repository: RuleRepository) : ViewModel() {
    val allRules: LiveData<List<Rule>> = repository.allRules
}

class RuleListViewModelFactory(private val repository: RuleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RuleListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}