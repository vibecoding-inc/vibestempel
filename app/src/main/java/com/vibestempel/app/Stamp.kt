package com.vibestempel.app

import java.io.Serializable

data class Stamp(
    val eventId: String,
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
