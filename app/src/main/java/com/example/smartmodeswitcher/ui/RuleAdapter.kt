package com.example.smartmodeswitcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmodeswitcher.R
import com.example.smartmodeswitcher.data.Rule
import com.example.smartmodeswitcher.util.convertDayNumberToJapanese

class RuleAdapter(
    private val onEnabledChanged: ((Rule, Boolean) -> Unit)? = null
) : ListAdapter<Rule, RuleAdapter.RuleViewHolder>(RuleDiffCallback()) {

    class RuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textStartTime: TextView = view.findViewById(R.id.textStartTime)
        val textEndTime: TextView = view.findViewById(R.id.textEndTime)
        val textMode: TextView = view.findViewById(R.id.textMode)
        val textDays: TextView = view.findViewById(R.id.textDays)
        val switchEnabled: Switch = view.findViewById(R.id.switchEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rule, parent, false)
        return RuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        val rule = getItem(position)
        holder.textStartTime.text = rule.startTime
        holder.textEndTime.text = rule.endTime
        holder.textMode.text = rule.mode.toString()
        // 曜日を日本語表記に変換して表示
        holder.textDays.text = convertDayNumberToJapanese(rule.days)
        holder.switchEnabled.isChecked = rule.enabled

        // スイッチのリスナーをセット
        holder.switchEnabled.setOnCheckedChangeListener(null) // リサイクル時の多重リスナー防止
        holder.switchEnabled.isChecked = rule.enabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onEnabledChanged?.invoke(rule, isChecked)
        }
    }
}

class RuleDiffCallback : DiffUtil.ItemCallback<Rule>() {
    override fun areItemsTheSame(oldItem: Rule, newItem: Rule): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Rule, newItem: Rule): Boolean {
        return oldItem == newItem
    }
}