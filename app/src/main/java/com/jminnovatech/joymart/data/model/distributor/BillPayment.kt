package com.jminnovatech.joymart.data.model.distributor

// ==============================
// BILL PAYMENT API RESPONSE
// ==============================

data class BillPaymentResponse(
    val success: Boolean,
    val data: BillPagination?,
    val total_due: Double
)

// ==============================
// PAGINATION
// ==============================

data class BillPagination(

    val current_page: Int = 1,

    val data: List<BillPaymentItem> = emptyList(),

    val last_page: Int = 1,

    val per_page: Int = 10,

    val total: Int = 0
)

// ==============================
// SINGLE BILL ITEM
// ==============================

data class BillPaymentItem(

    val id: Int,

    val bill_no: String = "",

    val buyer_name: String = "",

    val total: Double = 0.0,

    val due_amount: Double = 0.0,

    val paid_amount: Double = 0.0,

    val payment_status: String? = null,

    val created_at: String? = null
)

// ==============================
// PAYMENT HISTORY RESPONSE
// ==============================

data class PaymentHistoryResponse(

    val success: Boolean,

    val data: List<PaymentHistoryItem> = emptyList()
)

// ==============================
// SINGLE PAYMENT HISTORY ITEM
// ==============================

data class PaymentHistoryItem(

    val id: Int,

    val amount: Double = 0.0,

    val method: String = "",

    val utr_no: String? = null,

    val status: String = "",

    val reject_reason: String? = null,

    val created_at: String? = null
)