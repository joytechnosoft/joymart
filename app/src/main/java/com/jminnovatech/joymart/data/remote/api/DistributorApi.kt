package com.jminnovatech.joymart.data.remote.api

import com.jminnovatech.joymart.data.model.common.ApiResponse
import com.jminnovatech.joymart.data.model.distributor.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DistributorApi {

    // =========================
    // ORDERS
    // =========================

    @GET("distributor/orders")
    suspend fun getOrders(): ApiResponse<List<DistributorOrder>>

    @POST("distributor/orders/{id}/accept")
    suspend fun acceptOrder(
        @Path("id") orderId: Int
    ): ApiResponse<Any>

    @POST("distributor/orders/{id}/reject")
    suspend fun rejectOrder(
        @Path("id") orderId: Int
    ): ApiResponse<Any>



    // =========================
    // PRODUCTS
    // =========================

    // Laravel paginate() return করবে:
    // { success:true, data:{ data:[], current_page, last_page ... } }

    @GET("products")
    suspend fun getProducts(): ApiResponse<DistributorProductList>



    // =========================
    // ADD PRODUCT
    // =========================

    @Multipart
    @POST("products")
    suspend fun addProduct(
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("base_price") basePrice: RequestBody,
        @Part("sell_price") sellPrice: RequestBody,
        @Part("stock_qty") stockQty: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part image: MultipartBody.Part?
    ): ApiResponse<DistributorProductResponse>


    @Multipart
    @POST("products/{id}?_method=PUT")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("base_price") basePrice: RequestBody,
        @Part("sell_price") sellPrice: RequestBody,
        @Part("stock_qty") stockQty: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part image: MultipartBody.Part?
    ): ApiResponse<DistributorProductResponse>




    // =========================
    // DELETE PRODUCT
    // =========================

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): ApiResponse<Any>



    // =========================
    // CATEGORY (for dropdown)
    // =========================

    @GET("categories")
    suspend fun getCategories(): ApiResponse<List<DistributorCategory>>
}
