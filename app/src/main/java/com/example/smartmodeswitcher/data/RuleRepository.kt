package com.example.smartmodeswitcher.data

import androidx.lifecycle.LiveData

class RuleRepository(private val ruleDao: RuleDao) {
    val allRules: LiveData<List<Rule>> = ruleDao.getAll()

    suspend fun insert(rule: Rule) {
        ruleDao.insert(rule)
    }

    suspend fun update(rule: Rule) {
        ruleDao.update(rule)
    }

    suspend fun delete(rule: Rule) {
        ruleDao.delete(rule)
    }
}