package com.jminnovatech.joymart.data.model.customer

data class CustomerOrderCreateRequest(
    val buyer_name: String,
    val buyer_phone: String,
    val buyer_address: String,
    val items: List<CustomerOrderItemRequest>
)
