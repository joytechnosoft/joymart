package com.jminnovatech.data.model.customer

import com.jminnovatech.joymart.data.model.customer.CustomerOrderItem


data class CustomerOrder(
    val id: Int,
    val order_no: String,
    val total_amount: String,
    val status: String,
    val created_at: String,
    val delivery_info: String, // JSON string
    val items: List<CustomerOrderItem>
)
