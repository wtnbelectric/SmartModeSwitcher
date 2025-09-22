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
        // Convert to 1-based index where 1=Sunday, 2=Monday, ..., 7=Saturday
        // to match the days string format (e.g., "1111111" where each digit represents a day from Sunday to Saturday)
        val dayOfWeek = if (date.dayOfWeek.value == 7) 1 else date.dayOfWeek.value + 1
        viewModelScope.launch {
            val result = repository.getRulesByDayOfWeek(dayOfWeek)
            _rules.postValue(result)
        }
    }

    fun updateRule(rule: Rule) {
        viewModelScope.launch {
            repository.update(rule)
        }
    }
}