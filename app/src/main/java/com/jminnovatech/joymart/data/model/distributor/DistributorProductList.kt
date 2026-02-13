package com.jminnovatech.joymart.data.model.distributor

data class DistributorProductList(
    val data: List<DistributorProduct>,
    val current_page: Int,
    val last_page: Int
)
