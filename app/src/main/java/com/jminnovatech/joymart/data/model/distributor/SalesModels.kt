package com.jminnovatech.joymart.data.model.distributor

data class SalesPagination(
    val data: List<BillItem>,
    val current_page: Int,
    val last_page: Int
)

data class BillItem(
    val id: Int,
    val bill_no: String,
    val buyer_name: String?,
    val total: Double
)