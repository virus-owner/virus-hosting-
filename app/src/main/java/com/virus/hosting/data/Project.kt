package com.virus.hosting.data

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val language: String = "unknown",
    val port: Int = 0,
    val autoStart: Boolean = false,
    val running: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    val size: Long = 0
)
