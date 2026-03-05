package com.jminnovatech.joymart.ui.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jminnovatech.joymart.data.model.distributor.DashboardSummary
import com.jminnovatech.joymart.data.remote.api.RetrofitClient

@Composable
fun DistributorDashboardScreen() {

    val api = RetrofitClient.distributorApi

    var dashboard by remember { mutableStateOf<DashboardSummary?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        try {

            val res = api.getDashboardSummary()

            if (res.success) {
                dashboard = res.data
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF6F9FF), Color(0xFFE8F0FF))
                )
            )
    ) {

        if (loading) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

        } else {

            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {

                    Text(
                        "Distributor Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        DashboardCard(
                            "Products",
                            dashboard?.total_products.toString(),
                            Icons.Default.Inventory
                        )

                        DashboardCard(
                            "Orders",
                            dashboard?.total_orders.toString(),
                            Icons.Default.ShoppingCart
                        )
                    }
                }

                item {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        DashboardCard(
                            "Sales",
                            "₹${dashboard?.total_sales}",
                            Icons.Default.AttachMoney
                        )

                        DashboardCard(
                            "Profit",
                            "₹${dashboard?.total_profit}",
                            Icons.Default.TrendingUp
                        )
                    }
                }

                item {

                    DashboardCard(
                        "Low Stock",
                        dashboard?.low_stock.toString(),
                        Icons.Default.Warning
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector
) {

    Card(
        modifier = Modifier

            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(icon, null)

            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(title, color = Color.Gray)
        }
    }
}