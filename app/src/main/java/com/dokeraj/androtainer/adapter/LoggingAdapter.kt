package com.dokeraj.androtainer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.models.LogItem
import kotlinx.android.synthetic.main.logging_card_item.view.*

class LoggingAdapter(private val logList: List<LogItem>) : RecyclerView.Adapter<LoggingAdapter.LoggingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggingViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.logging_card_item, parent, false)

        return LoggingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LoggingViewHolder, position: Int) {
        val currentItem = logList[position]
        holder.tvLogLine.text = currentItem.logLine
    }

    override fun getItemCount() = logList.size

    class LoggingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLogLine: TextView = itemView.tvLoggingItem

    }
}