package com.example.smartmodeswitcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmodeswitcher.R
import com.example.smartmodeswitcher.data.Rule

class RuleAdapter(
    private var rules: List<Rule>
) : RecyclerView.Adapter<RuleAdapter.RuleViewHolder>() {

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
        val rule = rules[position]
        holder.textStartTime.text = rule.startTime
        holder.textEndTime.text = rule.endTime
        holder.textMode.text = rule.mode.toString()
        holder.textDays.text = rule.days
        holder.switchEnabled.isChecked = rule.enabled
    }

    override fun getItemCount(): Int = rules.size

    fun submitList(newRules: List<Rule>) {
        rules = newRules
        notifyDataSetChanged()
    }
}