package com.jminnovatech.joymart.ui.distributor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jminnovatech.joymart.data.model.distributor.DistributorOrder
import com.jminnovatech.joymart.data.repository.DistributorRepository
import kotlin.collections.emptyList

@Composable
fun DistributorHome() {

    val repo = DistributorRepository()

    var orders by remember { mutableStateOf<List<DistributorOrder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val res = repo.getOrders()
        if (res.success) {
            orders = res.data ?: emptyList()
        }
        loading = false
    }

    if (loading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Customer: ${order.customer_name}")
                        Text("Amount: ₹${order.total_amount}")
                        Text("Status: ${order.status}")
                    }
                }
            }
        }
    }
}
