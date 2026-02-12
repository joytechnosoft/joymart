package com.jminnovatech.joymart.data.model.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
