package com.jminnovatech.joymart.data.model.distributor


data class DistributorProduct(
    val id: Int,
    val title: String,
    val description: String?,
    val base_price: Double,
    val sell_price: Double,
    val stock_qty: Int,
    val mrp: Double?,                 // ✅ ADD THIS
    val discount_percent: Double?,    // ✅ ADD THIS (API already returns)
    val unit: String,
    val image_url: String?,
    val category_id: Int?   // ✅ MUST ADD THIS
)

data class DistributorProductList(
    val data: List<DistributorProduct>,
    val current_page: Int,
    val last_page: Int
)
data class DistributorProductResponse(
    val product: DistributorProduct
)
data class DistributorCategory(
    val id: Int,
    val name: String
)
