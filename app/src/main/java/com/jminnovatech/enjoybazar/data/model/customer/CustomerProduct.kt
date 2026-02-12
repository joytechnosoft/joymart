package com.jminnovatech.joymart.data.model.customer

data class CustomerProduct(
    val id: Int,
    val title: String,
    val sell_price: Double,
    val unit: String,
    val stock_qty: Double,
    val image: String?,
    val description: String? = null,
    val mrp: Double? = null,
    val discount_percent: Double? = null
)
