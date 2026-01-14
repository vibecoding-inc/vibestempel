package com.vibestempel.app

import java.io.Serializable

data class Stamp(
    val eventId: String,
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class UserStampCount(
    val userId: String,
    val deviceId: String,
    val username: String? = null,
    val stampCount: Int,
    val lastStampCollected: String? = null
)
