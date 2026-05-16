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
// =========================
// DASHBOARD
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
        @Part("mrp") mrp: RequestBody?,          // ✅ ADD THIS
        @Part("stock_qty") stockQty: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part image: MultipartBody.Part?         // ✅ IMPORTANT
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
        @Part("mrp") mrp: RequestBody?,          // ✅ ADD THIS
        @Part("stock_qty") stockQty: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part image: MultipartBody.Part?         // ✅ IMPORTANT
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

    @Multipart
    @POST("categories")
    suspend fun addCategory(
        @Part("name") name: RequestBody
    ): ApiResponse<DistributorCategory>


    @GET("products/export/pdf")
    @Streaming
    suspend fun exportProductsPdf(): okhttp3.ResponseBody

    @GET("products/barcodes/export/pdf")
    @Streaming
    suspend fun exportBarcodePdf(): okhttp3.ResponseBody

    @GET("products/{id}/barcode")
    @Streaming
    suspend fun exportSingleBarcode(
        @Path("id") id: Int
    ): okhttp3.ResponseBody
    @GET("admin/summary")
    suspend fun getDashboardSummary(): DashboardSummary

    @GET("admin/sales-chart")
    suspend fun getSalesChart(): ApiResponse<List<SalesChartItem>>


    @GET("admin/low-stock")
    suspend fun getLowStock(): ApiResponse<List<LowStockProduct>>



    @GET("admin/recent-orders")
    suspend fun getRecentOrders(): ApiResponse<List<RecentOrder>>

    @GET("admin/profit-analytics")
    suspend fun getProfitAnalytics(): ApiResponse<List<ProfitItem>>

    @GET("sales")
    suspend fun getSales(): ApiResponse<SalesPagination>

    @FormUrlEncoded
    @POST("sales")
    suspend fun createSale(
        @Field("buyer_name") buyerName:String,
        @Field("buyer_phone") buyerPhone:String,
        @Field("buyer_address") buyerAddress:String,
        @Field("discount") discount:Double,
        @Field("tax") tax:Double,
        @Field("paid_amount") paid:Double,
        @Field("items") items:String
    ): CreateSaleResponse
    @GET("distributor/bill-payments")
    suspend fun getBillPayments(
        @Query("status") status: String = "due",
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 50
    ): BillPaymentResponse
    @FormUrlEncoded
    @POST("distributor/bill-payments")
    suspend fun submitPayment(

        @Field("sale_id") saleId: Int,

        @Field("amount") amount: Double,

        @Field("method") method: String,

        @Field("need_approval") needApproval: Int,

        @Field("utr_no") utrNo: String?
    ): ApiResponse<Any>

    // =========================
// PAYMENT VERIFICATION
// =========================

    @GET("admin/payments/pending")
    suspend fun getPendingPayments():
            PendingPaymentResponse

    @POST("admin/payments/{id}/approve")
    suspend fun approvePayment(
        @Path("id") id: Int
    ): ApiResponse<Any>

    @FormUrlEncoded
    @POST("admin/payments/{id}/reject")
    suspend fun rejectPayment(

        @Path("id") id: Int,

        @Field("reason") reason: String
    ): ApiResponse<Any>

    @GET("admin/payments/{saleId}/history")
    suspend fun paymentHistory(
        @Path("saleId") saleId: Int
    ): PaymentHistoryResponse

    @GET("distributor/orders")
    suspend fun getCustomerOrders(
        @Query("status") status: String = "pending"
    ): CustomerOrdersResponse


    @FormUrlEncoded
    @POST("distributor/orders/{id}/accept")
    suspend fun acceptCustomerOrder(

        @Path("id") orderId: Int,

        @Field("paid_amount") paidAmount: Double

    ): AcceptOrderResponse


    @POST("distributor/orders/{id}/reject")
    suspend fun rejectCustomerOrder(
        @Path("id") orderId: Int
    ): ApiResponse<Any>

    @GET("distributor/profile")
    suspend fun getDistributorProfile():
            DistributorProfileResponse


    @FormUrlEncoded
    @POST("distributor/profile/update")
    suspend fun updateDistributorProfile(

        @Field("name") name: String,

        @Field("phone") phone: String,

        @Field("address") address: String,

        @Field("upi_id") upiId: String,

        @Field("consignor_name") consignorName: String,

        @Field("latitude") latitude: String,

        @Field("longitude") longitude: String,

        @Field("upi_qr_url") upiQrUrl: String

    ): ApiResponse<Any>
}
