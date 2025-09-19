package com.example.smartmodeswitcher.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.databinding.ItemRuleBinding

class RuleListAdapter : ListAdapter<Rule, RuleListAdapter.RuleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RuleViewHolder(private val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule) {
            binding.textStartTime.text = "${rule.startTime}"
            binding.textEndTime.text = " ${rule.endTime}"
            binding.textMode.text = when (rule.mode) {
                1 -> "通常"
                2 -> "バイブ"
                3 -> "サイレント"
                else -> "不明"
            }
            binding.switchEnabled.isChecked = rule.enabled
            // 編集・削除ボタンのリスナーもここで設定

            val daysMap = listOf("日", "月", "火", "水", "木", "金", "土")
            val daysStr = rule.days
                .mapIndexedNotNull { i: Int, c: Char -> if (c == '1') daysMap[i] else null }
                .joinToString("")
            binding.textDays.text = if (daysStr.isNotEmpty()) daysStr else "指定なし"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Rule>() {
        override fun areItemsTheSame(oldItem: Rule, newItem: Rule) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Rule, newItem: Rule) = oldItem == newItem
    }
}