package com.vibestempel.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class UserStampCountAdapter : RecyclerView.Adapter<UserStampCountAdapter.ViewHolder>() {
    
    private var userCounts = listOf<UserStampCount>()
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userDeviceId: TextView = view.findViewById(R.id.userDeviceId)
        val userName: TextView = view.findViewById(R.id.userName)
        val lastStamp: TextView = view.findViewById(R.id.lastStamp)
        val stampCount: TextView = view.findViewById(R.id.stampCount)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_stamp_count, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userCount = userCounts[position]
        
        // Display device ID or username
        if (!userCount.username.isNullOrBlank()) {
            holder.userDeviceId.text = userCount.username
            holder.userName.visibility = View.VISIBLE
            holder.userName.text = "Device: ${userCount.deviceId.take(8)}..."
        } else {
            holder.userDeviceId.text = "User-${userCount.deviceId.take(8)}"
            holder.userName.visibility = View.GONE
        }
        
        // Display stamp count
        holder.stampCount.text = userCount.stampCount.toString()
        
        // Display last stamp time
        if (userCount.lastStampCollected != null && userCount.stampCount > 0) {
            val timeAgo = getTimeAgo(userCount.lastStampCollected)
            holder.lastStamp.text = "Last stamp: $timeAgo"
            holder.lastStamp.visibility = View.VISIBLE
        } else {
            holder.lastStamp.visibility = View.GONE
        }
    }
    
    override fun getItemCount() = userCounts.size
    
    fun updateUsers(newCounts: List<UserStampCount>) {
        userCounts = newCounts
        notifyDataSetChanged()
    }
    
    private fun getTimeAgo(timestamp: String): String {
        return try {
            val instant = java.time.Instant.parse(timestamp)
            val now = java.time.Instant.now()
            val duration = java.time.Duration.between(instant, now)
            
            when {
                duration.toMinutes() < 1 -> "just now"
                duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
                duration.toHours() < 24 -> "${duration.toHours()} hours ago"
                else -> "${duration.toDays()} days ago"
            }
        } catch (e: Exception) {
            "recently"
        }
    }
}
