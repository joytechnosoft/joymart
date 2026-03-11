package com.jminnovatech.joymart.data.model.distributor

/* ---------------- SALES LIST PAGINATION ---------------- */

data class SalesPagination(
    val data: List<BillItem>,
    val current_page: Int,
    val last_page: Int
)

/* ---------------- BILL LIST ITEM ---------------- */

data class BillItem(
    val id: Int,
    val bill_no: String,
    val buyer_name: String?,
    val total: Double
)

/* ---------------- CREATE SALE RESPONSE ---------------- */

data class CreateSaleResponse(
    val success: Boolean,
    val data: SaleData
)

/* ---------------- SALE DATA ---------------- */

data class SaleData(
    val id: Int,
    val bill_no: String
)