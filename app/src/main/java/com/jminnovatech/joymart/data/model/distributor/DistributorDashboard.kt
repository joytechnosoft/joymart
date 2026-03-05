package com.jminnovatech.joymart.data.model.distributor

data class DistributorDashboard(
    val total_products: Int,
    val total_orders: Int,
    val total_profit: Double,
    val low_stock: Int
)