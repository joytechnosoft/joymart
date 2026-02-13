package com.jminnovatech.joymart.data.repository

import com.jminnovatech.joymart.data.model.customer.CustomerOrderCreateRequest
import com.jminnovatech.joymart.data.remote.api.RetrofitClient

class CustomerRepository {

    // ---------------- PRODUCTS ----------------
    suspend fun getProducts() =
        RetrofitClient.customerApi.getProducts()

    // ---------------- ORDERS ----------------
    suspend fun getOrders() =
        RetrofitClient.customerApi.getOrders()

    suspend fun placeOrder(
        req: CustomerOrderCreateRequest
    ) =
        RetrofitClient.customerApi.placeOrder(req)

    // ---------------- PROFILE ----------------

    suspend fun getCustomerProfile() =
        RetrofitClient.customerApi.getCustomerProfile()

    suspend fun updateCustomerProfile(
        name: String,
        phone: String,
        address: String
    ) =
        RetrofitClient.customerApi.updateCustomerProfile(
            mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address
            )
        )
}
