package com.example.smartmodeswitcher.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules")
    fun getAll(): LiveData<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: Rule)

    @Update
    suspend fun update(rule: Rule)

    @Delete
    suspend fun delete(rule: Rule)

    // RuleDao.kt
    @Query("SELECT * FROM rules WHERE enabled = 1 AND SUBSTR(days, :dayOfWeek, 1) = '1'")
    suspend fun getRulesByDayOfWeek(dayOfWeek: Int): List<Rule>
}