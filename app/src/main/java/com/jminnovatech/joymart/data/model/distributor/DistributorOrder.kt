package com.jminnovatech.joymart.data.model.distributor

data class DistributorOrder(
    val id: Int,
    val customer_name: String,
    val total_amount: Double,
    val status: String
)
