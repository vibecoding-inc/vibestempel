package com.vibestempel.app

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Storage implementation using MCP server for all backend operations
 * This provides maximum security by keeping all database logic server-side
 */
class MCPStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "MCPStorage"
    }
    
    private val mcpClient = MCPClient(context)
    
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    
    // Realtime flows for admin dashboard
    private val _userStampCounts = MutableStateFlow<List<UserStampCount>>(emptyList())
    val userStampCounts: Flow<List<UserStampCount>> = _userStampCounts.asStateFlow()
    
    /**
     * Get the device ID used to identify this user
     */
    fun getDeviceId(): String = deviceId
    
    /**
     * Create an event via MCP server
     */
    suspend fun createEvent(event: Event): Result<Boolean> {
        return try {
            val eventData = JSONObject().apply {
                put("id", event.id)
                put("name", event.name)
                put("description", event.description)
                put("created_by", "admin")
                put("is_active", true)
            }
            
            val result = mcpClient.insert("events", eventData)
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to create event"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all active events via MCP server
     */
    suspend fun getActiveEvents(): Result<List<Event>> {
        return try {
            val filters = mapOf("is_active" to true)
            val result = mcpClient.query("events", "*", filters)
            
            if (result.isSuccess) {
                val events = mutableListOf<Event>()
                val jsonArray = result.getOrNull() ?: JSONArray()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    events.add(
                        Event(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            timestamp = parseTimestamp(obj.optString("created_at"))
                        )
                    )
                }
                
                Result.success(events)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get events"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a stamp via MCP server (uses serverside function for duplicate checking)
     */
    suspend fun addStamp(eventId: String, eventName: String): Result<Boolean> {
        return try {
            val params = mapOf(
                "p_device_id" to deviceId,
                "p_event_id" to eventId,
                "p_event_name" to eventName
            )
            
            val result = mcpClient.callFunction("add_stamp", params)
            
            if (result.isSuccess) {
                val response = result.getOrNull()
                val success = response?.optBoolean("success", false) ?: false
                Result.success(success)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to add stamp"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding stamp", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get stamps for the current device/user via MCP server
     */
    suspend fun getStamps(): Result<List<Stamp>> {
        return try {
            // First get user ID
            val userId = getOrCreateUserId()
            if (userId == null) {
                return Result.failure(Exception("Could not get user ID"))
            }
            
            val filters = mapOf("user_id" to userId)
            val result = mcpClient.query("stamps", "*", filters, "collected_at.desc")
            
            if (result.isSuccess) {
                val stamps = mutableListOf<Stamp>()
                val jsonArray = result.getOrNull() ?: JSONArray()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    stamps.add(
                        Stamp(
                            eventId = obj.getString("event_id"),
                            eventName = obj.getString("event_name"),
                            timestamp = parseTimestamp(obj.optString("collected_at"))
                        )
                    )
                }
                
                Result.success(stamps)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get stamps"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stamps", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if the current user has a stamp for a specific event
     */
    suspend fun hasStampForEvent(eventId: String): Result<Boolean> {
        return try {
            val userId = getOrCreateUserId()
            if (userId == null) {
                return Result.success(false)
            }
            
            val filters = mapOf(
                "user_id" to userId,
                "event_id" to eventId
            )
            val result = mcpClient.query("stamps", "id", filters)
            
            if (result.isSuccess) {
                val jsonArray = result.getOrNull() ?: JSONArray()
                Result.success(jsonArray.length() > 0)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stamp", e)
            Result.success(false)
        }
    }
    
    /**
     * Get all users' stamp counts via MCP server (for admin dashboard)
     */
    suspend fun getUserStampCounts(): Result<List<UserStampCount>> {
        return try {
            val result = mcpClient.query("user_stamp_counts", "*")
            
            if (result.isSuccess) {
                val counts = mutableListOf<UserStampCount>()
                val jsonArray = result.getOrNull() ?: JSONArray()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    counts.add(
                        UserStampCount(
                            userId = obj.getString("user_id"),
                            deviceId = obj.getString("device_id"),
                            username = obj.optString("username"),
                            stampCount = obj.getInt("stamp_count"),
                            lastStampCollected = obj.optString("last_stamp_collected")
                        )
                    )
                }
                
                Result.success(counts)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get user counts"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user stamp counts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Subscribe to realtime updates for user stamp counts
     */
    suspend fun subscribeToUserStampCounts() {
        try {
            val result = mcpClient.subscribe("stamps", "*") { update ->
                // When stamps change, refresh the counts
                kotlinx.coroutines.MainScope().launch {
                    refreshUserStampCounts()
                }
            }
            
            if (result.isSuccess) {
                Log.d(TAG, "Subscribed to realtime updates")
            }
            
            // Initial load
            refreshUserStampCounts()
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to updates", e)
        }
    }
    
    /**
     * Refresh the user stamp counts
     */
    private suspend fun refreshUserStampCounts() {
        val result = getUserStampCounts()
        if (result.isSuccess) {
            _userStampCounts.value = result.getOrDefault(emptyList())
        }
    }
    
    /**
     * Get or create user ID for the current device via MCP server
     */
    private suspend fun getOrCreateUserId(): String? {
        return try {
            // Try to find existing user
            val filters = mapOf("device_id" to deviceId)
            val result = mcpClient.query("users", "*", filters)
            
            if (result.isSuccess) {
                val jsonArray = result.getOrNull() ?: JSONArray()
                
                if (jsonArray.length() > 0) {
                    // User exists
                    jsonArray.getJSONObject(0).getString("id")
                } else {
                    // Create new user
                    val userData = JSONObject().apply {
                        put("device_id", deviceId)
                        put("username", "User-${deviceId.take(8)}")
                    }
                    
                    val insertResult = mcpClient.insert("users", userData)
                    if (insertResult.isSuccess) {
                        insertResult.getOrNull()?.getString("id")
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating user", e)
            null
        }
    }
    
    /**
     * Parse timestamp string to Long (milliseconds)
     */
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp.isNullOrEmpty()) return System.currentTimeMillis()
        
        return try {
            // Try parsing ISO 8601 format
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    // Admin token methods - kept for backward compatibility
    private val KEY_ADMIN_TOKEN = "admin_token"
    private val DEFAULT_ADMIN_TOKEN = "admin123"
    
    fun validateAdminToken(token: String): Boolean {
        val prefs = context.getSharedPreferences("vibestempel_prefs", Context.MODE_PRIVATE)
        val savedToken = prefs.getString(KEY_ADMIN_TOKEN, DEFAULT_ADMIN_TOKEN)
        return token == savedToken
    }
    
    fun setAdminToken(token: String) {
        val prefs = context.getSharedPreferences("vibestempel_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ADMIN_TOKEN, token).apply()
    }
    
    fun getAdminToken(): String {
        val prefs = context.getSharedPreferences("vibestempel_prefs", Context.MODE_PRIVATE)
        return prefs.getString(KEY_ADMIN_TOKEN, DEFAULT_ADMIN_TOKEN) ?: DEFAULT_ADMIN_TOKEN
    }
}
