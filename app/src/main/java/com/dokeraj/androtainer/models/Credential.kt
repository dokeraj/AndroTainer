package com.dokeraj.androtainer.models

data class Credential(
    val serverUrl: String,
    val username: String,
    val pwd: String,
    val jwt: String? = null,
    val jwtValidUntil: Long? = null,
    val lastActivity: Long? = null,
)
