package com.example.smartmodeswitcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartmodeswitcher.data.RuleRepository
import com.example.smartmodeswitcher.ui.DashboardViewModel

class DashboardViewModelFactory(
    private val repository: RuleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}