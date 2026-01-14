package com.vibestempel.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    val username: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class SupabaseEvent(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class SupabaseStamp(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("event_name")
    val eventName: String,
    @SerialName("collected_at")
    val collectedAt: String? = null
)

@Serializable
data class UserStampCount(
    @SerialName("user_id")
    val userId: String,
    @SerialName("device_id")
    val deviceId: String,
    val username: String? = null,
    @SerialName("stamp_count")
    val stampCount: Int,
    @SerialName("last_stamp_collected")
    val lastStampCollected: String? = null
)

@Serializable
data class AddStampResult(
    val success: Boolean,
    @SerialName("stamp_id")
    val stampId: String? = null,
    val message: String
)
