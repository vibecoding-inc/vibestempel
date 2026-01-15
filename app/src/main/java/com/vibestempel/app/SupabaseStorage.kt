package com.vibestempel.app

import android.content.Context
import android.provider.Settings
import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Storage implementation using Supabase directly
 * This provides serverside storage with realtime capabilities
 */
class SupabaseStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "SupabaseStorage"
        private const val KEY_ADMIN_TOKEN = "admin_token"
        private const val DEFAULT_ADMIN_TOKEN = "admin123"
    }
    
    private val client = SupabaseClient.client
    
    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    
    // Realtime flows for admin dashboard
    private val _userStampCounts = MutableStateFlow<List<UserStampCount>>(emptyList())
    val userStampCounts: Flow<List<UserStampCount>> = _userStampCounts.asStateFlow()
    
    /**
     * Create an event in Supabase
     */
    suspend fun createEvent(event: Event): Result<SupabaseEvent> {
        return try {
            val supabaseEvent = SupabaseEvent(
                id = event.id,
                name = event.name,
                description = event.description,
                createdBy = "admin",
                isActive = true
            )
            
            val created = client.from("events")
                .insert(supabaseEvent) {
                    select()
                }
                .decodeSingle<SupabaseEvent>()
            
            Result.success(created)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all active events
     */
    suspend fun getActiveEvents(): Result<List<SupabaseEvent>> {
        return try {
            val events = client.from("events")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                }
                .decodeList<SupabaseEvent>()
            
            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a stamp for the current device/user
     */
    suspend fun addStamp(eventId: String, eventName: String): Result<Boolean> {
        return try {
            val userId = getOrCreateUserId()
            
            val stamp = SupabaseStamp(
                userId = userId,
                eventId = eventId,
                eventName = eventName
            )
            
            client.from("stamps")
                .insert(stamp)
            
            Result.success(true)
        } catch (e: Exception) {
            // Check if it's a duplicate error
            if (e.message?.contains("duplicate", ignoreCase = true) == true ||
                e.message?.contains("unique", ignoreCase = true) == true) {
                Result.success(false) // Already have this stamp
            } else {
                Log.e(TAG, "Error adding stamp", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get stamps for the current device/user
     */
    suspend fun getStamps(): Result<List<Stamp>> {
        return try {
            val userId = getOrCreateUserId()
            
            val stamps = client.from("stamps")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SupabaseStamp>()
                .sortedByDescending { it.collectedAt }
            
            // Convert to local Stamp model
            val localStamps = stamps.map { supabaseStamp ->
                Stamp(
                    eventId = supabaseStamp.eventId,
                    eventName = supabaseStamp.eventName,
                    timestamp = parseTimestamp(supabaseStamp.collectedAt)
                )
            }
            
            Result.success(localStamps)
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
            
            val stamps = client.from("stamps")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("event_id", eventId)
                    }
                }
                .decodeList<SupabaseStamp>()
            
            Result.success(stamps.isNotEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stamp", e)
            Result.success(false)
        }
    }
    
    /**
     * Get all users' stamp counts (for admin dashboard)
     */
    suspend fun getUserStampCounts(): Result<List<UserStampCount>> {
        return try {
            val counts = client.from("user_stamp_counts")
                .select()
                .decodeList<UserStampCount>()
            
            Result.success(counts)
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
            val channel = client.realtime.channel("user-stamp-updates")
            
            // Subscribe to stamps table changes
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "stamps"
            }.onEach { change ->
                // Refresh the counts when stamps change
                refreshUserStampCounts()
            }.launchIn(CoroutineScope(Dispatchers.Main))
            
            channel.subscribe()
            
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
     * Get or create user ID for the current device
     */
    private suspend fun getOrCreateUserId(): String {
        val deviceId = getDeviceId()
        
        return try {
            // Try to find existing user
            val existingUsers = client.from("users")
                .select {
                    filter {
                        eq("device_id", deviceId)
                    }
                }
                .decodeList<SupabaseUser>()
            
            if (existingUsers.isNotEmpty()) {
                existingUsers.first().id ?: createNewUser()
            } else {
                createNewUser()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating user", e)
            createNewUser()
        }
    }
    
    /**
     * Create a new user for the current device
     */
    private suspend fun createNewUser(): String {
        val deviceId = getDeviceId()
        val newUser = SupabaseUser(
            deviceId = deviceId,
            username = "User-${deviceId.take(8)}"
        )
        
        val created = client.from("users")
            .insert(newUser) {
                select()
            }
            .decodeSingle<SupabaseUser>()
        
        return created.id ?: throw Exception("Failed to create user")
    }
    
    /**
     * Parse timestamp string to Long (milliseconds)
     */
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * Get the current user's username
     */
    suspend fun getUsername(): Result<String?> {
        return try {
            val deviceId = getDeviceId()
            
            val users = client.from("users")
                .select {
                    filter {
                        eq("device_id", deviceId)
                    }
                }
                .decodeList<SupabaseUser>()
            
            if (users.isNotEmpty()) {
                Result.success(users.first().username)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting username", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update the current user's username
     */
    suspend fun updateUsername(newUsername: String): Result<Boolean> {
        return try {
            val deviceId = getDeviceId()
            
            client.from("users")
                .update({
                    set("username", newUsername)
                }) {
                    filter {
                        eq("device_id", deviceId)
                    }
                }
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating username", e)
            Result.failure(e)
        }
    }
    
    // Admin token methods - kept for backward compatibility
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
