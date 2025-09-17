package com.example.smartmodeswitcher.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules")
    fun getAll(): LiveData<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: Rule): Long

    @Update
    suspend fun update(rule: Rule)

    @Delete
    suspend fun delete(rule: Rule)
}