package com.jminnovatech.joymart.util

import com.jminnovatech.joymart.data.model.auth.UserRole

fun mapRole(apiRole: String): UserRole {
    return when (apiRole.lowercase()) {
        "company" -> UserRole.COMPANY
        "sr_executive" -> UserRole.SR_EXECUTIVE
        "executive" -> UserRole.EXECUTIVE
        "distributor" -> UserRole.DISTRIBUTOR
        "customer" -> UserRole.CUSTOMER
        "user" -> UserRole.USER
        else -> UserRole.USER
    }
}
