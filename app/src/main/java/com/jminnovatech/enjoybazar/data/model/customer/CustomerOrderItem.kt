package com.jminnovatech.joymart.data.model.customer

data class CustomerOrderItem(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val product_name: String,
    val unit: String,
    val qty: String,
    val price: String,
    val total: String
)
