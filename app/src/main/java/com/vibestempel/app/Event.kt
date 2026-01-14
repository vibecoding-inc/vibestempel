package com.vibestempel.app

import java.io.Serializable

data class Event(
    val id: String,
    val name: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
