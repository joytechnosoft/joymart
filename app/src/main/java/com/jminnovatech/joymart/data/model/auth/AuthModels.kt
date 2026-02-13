package com.jminnovatech.joymart.data.remote.model

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val role: String?,
    val user: User?,
    val message: String? = null
)

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val role: String
)
