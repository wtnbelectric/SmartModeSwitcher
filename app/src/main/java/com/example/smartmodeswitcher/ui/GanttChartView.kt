package com.example.smartmodeswitcher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.smartmodeswitcher.data.Rule

class GanttChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var rules: List<Rule> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 32f
    }
    private var activeRuleId: Long? = null

    fun setRules(rules: List<Rule>) {
        this.rules = rules
        invalidate()
    }

    fun setActiveRuleId(ruleId: Long?) {
        activeRuleId = ruleId
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rules.isEmpty()) return

        val chartHeight = height.toFloat()
        val chartWidth = width.toFloat()
        val rowHeight = chartHeight / rules.size

        for ((i, rule) in rules.withIndex()) {
            // 時刻を0.0～24.0の小数に変換
            val start = timeToFloat(rule.startTime)
            val end = timeToFloat(rule.endTime)
            val left = chartWidth * (start / 24f)
            val right = chartWidth * (end / 24f)
            val top = i * rowHeight + rowHeight * 0.2f
            val bottom = (i + 1) * rowHeight - rowHeight * 0.2f

            // モードごとに色分け
            paint.color = when (rule.mode) {
                1 -> Color.parseColor("#2196F3") // 通常: 青
                2 -> Color.parseColor("#FF9800") // バイブ: オレンジ
                3 -> Color.parseColor("#F44336") // サイレント: 赤
                else -> Color.GRAY
            }
            // ハイライト
            if (rule.id.toLong() == activeRuleId) {
                paint.strokeWidth = 12f
                paint.color = Color.parseColor("#1976D2") // 濃い青
            } else {
                paint.strokeWidth = 6f
            }
            canvas.drawRect(left, top, right, bottom, paint)

            // ルール名や時刻を左に表示
            val label = "${rule.startTime}～${rule.endTime}"
            canvas.drawText(label, 8f, top + rowHeight * 0.6f, textPaint)
        }
    }

    private fun timeToFloat(time: String): Float {
        // "HH:mm" → 小数
        return try {
            val parts = time.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            h + m / 60f
        } catch (e: Exception) {
            0f
        }
    }
}
