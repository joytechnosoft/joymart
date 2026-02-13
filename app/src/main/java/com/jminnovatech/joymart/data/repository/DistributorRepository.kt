package com.jminnovatech.joymart.data.repository

import com.jminnovatech.joymart.data.remote.api.RetrofitClient

class DistributorRepository {

    suspend fun getOrders() =
        RetrofitClient.distributorApi.getOrders()

    suspend fun acceptOrder(id: Int) =
        RetrofitClient.distributorApi.acceptOrder(id)

    suspend fun rejectOrder(id: Int) =
        RetrofitClient.distributorApi.rejectOrder(id)
}
