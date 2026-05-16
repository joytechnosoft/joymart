package com.jminnovatech.joymart.data.model.distributor

data class CustomerOrdersResponse(

    val success: Boolean,

    val data: List<CustomerOrder> = emptyList()
)

data class CustomerOrder(

    val id: Int,

    val status: String = "",

    val total_amount: Double = 0.0,
    val bill_id: Int? = null,

    val bill_no: String? = null,

    val customer_name: String = "",

    val customer_phone: String = "",

    val customer_address: String = "",

    val items: List<CustomerOrderItem> = emptyList()
)

data class CustomerOrderItem(

    val product_id: Int,

    val product_name: String = "",

    val qty: Double = 0.0,

    val mrp: Double = 0.0,

    val price: Double = 0.0,

    val total: Double = 0.0
)



data class AcceptOrderResponse(
    val success: Boolean,

    val bill_no: String? = null,

    val message: String? = null
)