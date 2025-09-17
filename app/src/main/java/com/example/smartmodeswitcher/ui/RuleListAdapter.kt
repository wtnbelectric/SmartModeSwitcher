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
            binding.textViewTime.text = "${rule.startTime} - ${rule.endTime}"
            binding.textViewMode.text = when (rule.mode) {
                1 -> "通常"
                2 -> "バイブ"
                3 -> "サイレント"
                else -> "不明"
            }
            binding.switchEnabled.isChecked = rule.enabled
            // 編集・削除ボタンのリスナーもここで設定
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Rule>() {
        override fun areItemsTheSame(oldItem: Rule, newItem: Rule) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Rule, newItem: Rule) = oldItem == newItem
    }
}