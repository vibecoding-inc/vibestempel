package com.vibestempel.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StampsAdapter : RecyclerView.Adapter<StampsAdapter.StampViewHolder>() {
    
    private var stamps: List<Stamp> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
    
    fun updateStamps(newStamps: List<Stamp>) {
        stamps = newStamps.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stamp, parent, false)
        return StampViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        holder.bind(stamps[position])
    }
    
    override fun getItemCount(): Int = stamps.size
    
    inner class StampViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventNameText: TextView = itemView.findViewById(R.id.eventNameText)
        private val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        
        fun bind(stamp: Stamp) {
            eventNameText.text = stamp.eventName
            timestampText.text = dateFormat.format(Date(stamp.timestamp))
        }
    }
}
