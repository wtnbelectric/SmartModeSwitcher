package com.example.smartmodeswitcher.data

class RuleRepository(private val dao: RuleDao) {
    val allRules = dao.getAll()

    suspend fun insert(rule: Rule) {
        dao.insert(rule)
    }

    suspend fun update(rule: Rule) = dao.update(rule)
    suspend fun delete(rule: Rule) = dao.delete(rule)
}