package com.example.smartmodeswitcher.util

/**
 * 曜日を表す数字（1-7）を日本語の曜日名に変換する
 * 1: 日曜日, 2: 月曜日, ..., 7: 土曜日
 */
fun convertDayNumberToJapanese(days: String): String {
    val dayNames = listOf("日", "月", "火", "水", "木", "金", "土")
    return days.mapIndexed { index, c ->
        if (c == '1') dayNames[index] else ""
    }.filter { it.isNotEmpty() }.joinToString(",")
}
