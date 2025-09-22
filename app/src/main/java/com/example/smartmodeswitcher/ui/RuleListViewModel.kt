package com.example.smartmodeswitcher.ui

import androidx.lifecycle.*
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.data.RuleRepository
import kotlinx.coroutines.launch

class RuleListViewModel(private val repository: RuleRepository) : ViewModel() {
    val allRules = repository.allRules

    fun insert(rule: Rule) = viewModelScope.launch {
        repository.insert(rule)
    }

    fun delete(rule: Rule) = viewModelScope.launch {
        repository.delete(rule)
    }

    fun update(rule: Rule) = viewModelScope.launch {
        repository.update(rule)
    }

    fun updateRule(rule: com.example.smartmodeswitcher.data.Rule) {
        viewModelScope.launch {
            repository.update(rule)
        }
    }
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