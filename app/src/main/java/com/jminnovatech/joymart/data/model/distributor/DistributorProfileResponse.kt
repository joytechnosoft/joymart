package com.jminnovatech.joymart.data.model.distributor

data class DistributorProfileResponse(

    val success: Boolean,

    val data: DistributorProfile
)

data class DistributorProfile(

    val id: Int,

    val name: String = "",

    val email: String = "",

    val phone: String = "",

    val address: String = "",

    val role: String = "",

    val parent_id: Int? = null,

    val is_self_distributor: Int = 0,

    val can_buy_global: Int = 0,

    val wallet_balance: Double = 0.0,

    val package_active: Int = 0,

    val upi_qr_url: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val shop_logo: String? = null,

    val upi_id: String? = null,

    val consignor_name: String? = null,

    val created_at: String? = null
)