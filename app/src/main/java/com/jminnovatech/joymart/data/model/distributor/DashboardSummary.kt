package com.jminnovatech.joymart.data.model.distributor

data class DashboardSummary(

    val total_products: Int,

    val total_orders: Int,

    val total_sales: Double,

    val total_profit: Double,

    val low_stock: Int
)