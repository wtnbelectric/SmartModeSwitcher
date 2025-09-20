package com.example.smartmodeswitcher.ui

import androidx.lifecycle.*
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.data.RuleRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(private val repository: RuleRepository) : ViewModel() {
    private val _rules = MutableLiveData<List<Rule>>()
    val rules: LiveData<List<Rule>> = _rules

    fun loadRulesForDate(date: LocalDate) {
        val dayOfWeek = date.dayOfWeek.value % 7 // 日曜=0, 月曜=1...
        viewModelScope.launch {
            val result = repository.getRulesByDayOfWeek(dayOfWeek)
            _rules.postValue(result)
        }
    }
}