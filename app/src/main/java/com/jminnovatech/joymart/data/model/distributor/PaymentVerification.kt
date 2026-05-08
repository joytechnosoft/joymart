package com.jminnovatech.joymart.data.model.distributor

data class PendingPaymentResponse(
    val success: Boolean,
    val data: List<PendingPaymentItem> = emptyList()
)

data class PendingPaymentItem(

    val id: Int,

    val sale_id: Int,

    val bill_no: String = "",

    val buyer_name: String = "",

    val amount: Double = 0.0,

    val utr_no: String = ""
)