package com.jminnovatech.joymart.data.remote.api

import com.jminnovatech.data.model.customer.CustomerOrder
import com.jminnovatech.joymart.data.model.common.ApiResponse
import com.jminnovatech.joymart.data.model.customer.CustomerOrderCreateRequest
import com.jminnovatech.joymart.data.model.customer.CustomerProduct
import com.jminnovatech.joymart.data.model.customer.CustomerProfile
import retrofit2.http.*

interface CustomerApi {

    @GET("customer/products")
    suspend fun getProducts(): ApiResponse<List<CustomerProduct>>



    @GET("customer/my-orders")
    suspend fun getOrders(): ApiResponse<List<CustomerOrder>>





    @POST("customer/orders")
    suspend fun placeOrder(
        @Body request: CustomerOrderCreateRequest
    ): ApiResponse<Any>

    @POST("customer/address")
    suspend fun saveAddress(
        @Body body: Map<String, String>
    ): ApiResponse<Any>

    // -------- PROFILE --------

    @GET("customer/profile")
    suspend fun getCustomerProfile(): ApiResponse<CustomerProfile>

    @POST("customer/profile/update")
    suspend fun updateCustomerProfile(
        @Body body: Map<String, String>
    ): ApiResponse<Any>
}
