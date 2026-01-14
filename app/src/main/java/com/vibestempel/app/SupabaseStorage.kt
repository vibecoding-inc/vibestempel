package com.vibestempel.app

import android.content.Context
import android.provider.Settings
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseStorage(private val context: Context) {
    
    private val client = SupabaseClient.client
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    
    // Realtime flows
    private val _userStampCounts = MutableStateFlow<List<UserStampCount>>(emptyList())
    val userStampCounts: Flow<List<UserStampCount>> = _userStampCounts.asStateFlow()
    
    /**
     * Get the device ID used to identify this user
     */
    fun getDeviceId(): String = deviceId
    
    /**
     * Create an event in Supabase
     */
    suspend fun createEvent(event: Event): Result<SupabaseEvent> {
        return try {
            val supabaseEvent = SupabaseEvent(
                id = event.id,
                name = event.name,
                description = event.description,
                createdBy = "admin", // Can be enhanced with actual admin ID
                isActive = true
            )
            
            val created = client.from("events")
                .insert(supabaseEvent) {
                    select()
                }
                .decodeSingle<SupabaseEvent>()
            
            Result.success(created)
        } catch (e: Exception) {
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
            Result.failure(e)
        }
    }
    
    /**
     * Add a stamp for the current device/user
     * Uses the serverside add_stamp function for duplicate checking
     */
    suspend fun addStamp(eventId: String, eventName: String): Result<Boolean> {
        return try {
            // Call the add_stamp function
            val result = client.from("rpc")
                .select(
                    columns = Columns.raw(
                        """
                        add_stamp(
                            p_device_id := '$deviceId',
                            p_event_id := '$eventId',
                            p_event_name := '$eventName'
                        )
                        """.trimIndent()
                    )
                )
                .decodeSingle<AddStampResult>()
            
            Result.success(result.success)
        } catch (e: Exception) {
            // Fallback: Try direct insert
            try {
                // First get or create user
                val userId = getOrCreateUserId()
                
                val stamp = SupabaseStamp(
                    userId = userId,
                    eventId = eventId,
                    eventName = eventName
                )
                
                client.from("stamps")
                    .insert(stamp)
                
                Result.success(true)
            } catch (insertError: Exception) {
                // Check if it's a duplicate error
                if (insertError.message?.contains("unique", ignoreCase = true) == true) {
                    Result.success(false) // Already have this stamp
                } else {
                    Result.failure(insertError)
                }
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
                    order(column = "collected_at", ascending = false)
                }
                .decodeList<SupabaseStamp>()
            
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
            Result.failure(e)
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
            channel.postgresChangeFlow<SupabaseStamp>("public") {
                table = "stamps"
            }.collect { change ->
                // Refresh the counts when stamps change
                refreshUserStampCounts()
            }
            
            channel.subscribe()
            
            // Initial load
            refreshUserStampCounts()
        } catch (e: Exception) {
            e.printStackTrace()
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
            createNewUser()
        }
    }
    
    /**
     * Create a new user for the current device
     */
    private suspend fun createNewUser(): String {
        val newUser = SupabaseUser(
            deviceId = deviceId,
            username = "User-${deviceId.take(8)}" // Default username
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
            // Supabase returns ISO 8601 format timestamps
            // This is a simple parser - you may want to use a proper date library
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    // Admin token methods - kept for backward compatibility but should be moved to Supabase auth
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
