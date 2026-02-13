package com.jminnovatech.joymart.data.model.distributor


data class DistributorProduct(
    val id: Int,
    val title: String,
    val description: String?,
    val base_price: Double,
    val sell_price: Double,
    val stock_qty: Int,
    val unit: String,
    val image_url: String?,
    val category_id: Int?   // ✅ MUST ADD THIS
)

