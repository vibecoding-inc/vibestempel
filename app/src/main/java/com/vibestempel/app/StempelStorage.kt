package com.vibestempel.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StempelStorage(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("vibestempel_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_STAMPS = "stamps"
        private const val KEY_ADMIN_TOKEN = "admin_token"
        private const val DEFAULT_ADMIN_TOKEN = "admin123"
    }
    
    fun getStamps(): List<Stamp> {
        val json = prefs.getString(KEY_STAMPS, null) ?: return emptyList()
        val type = object : TypeToken<List<Stamp>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun addStamp(stamp: Stamp): Boolean {
        val stamps = getStamps().toMutableList()
        
        // Check if stamp for this event already exists
        if (stamps.any { it.eventId == stamp.eventId }) {
            return false
        }
        
        stamps.add(stamp)
        val json = gson.toJson(stamps)
        prefs.edit().putString(KEY_STAMPS, json).apply()
        return true
    }
    
    fun hasStampForEvent(eventId: String): Boolean {
        return getStamps().any { it.eventId == eventId }
    }
    
    fun validateAdminToken(token: String): Boolean {
        val savedToken = prefs.getString(KEY_ADMIN_TOKEN, DEFAULT_ADMIN_TOKEN)
        return token == savedToken
    }
    
    fun setAdminToken(token: String) {
        prefs.edit().putString(KEY_ADMIN_TOKEN, token).apply()
    }
    
    fun getAdminToken(): String {
        return prefs.getString(KEY_ADMIN_TOKEN, DEFAULT_ADMIN_TOKEN) ?: DEFAULT_ADMIN_TOKEN
    }
}
