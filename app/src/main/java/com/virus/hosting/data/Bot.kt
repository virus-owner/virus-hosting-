package com.virus.hosting.data

import java.util.UUID

data class Bot(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val token: String,
    val ownerId: Long,
    val enabled: Boolean = true,
    val created: Long = System.currentTimeMillis(),
    val running: Boolean = false
)
