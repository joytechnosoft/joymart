package com.jminnovatech.joymart.data.model.customer

data class CustomerProfile(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val address: String?
)
