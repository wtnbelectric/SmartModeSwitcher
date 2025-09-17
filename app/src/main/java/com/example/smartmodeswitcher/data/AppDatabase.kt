package com.example.smartmodeswitcher.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Rule::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
}