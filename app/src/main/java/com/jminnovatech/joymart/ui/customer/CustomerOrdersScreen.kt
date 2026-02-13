package com.jminnovatech.joymart.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jminnovatech.joymart.ui.customer.vm.CustomerViewModel


@Composable
fun CustomerOrdersScreen(vm: CustomerViewModel) {

    val orders by vm.orders.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadOrders()
    }

    if (orders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders yet", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {

        items(orders) { order ->

            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Column(Modifier.padding(16.dp)) {

                    // 🔹 HEADER
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(
                                "Order #${order.order_no}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "₹ ${order.total_amount}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        StatusBadge(order.status)
                    }

                    if (!expanded) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap to view details",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // 🔽 EXPANDED CONTENT
                    if (expanded) {

                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(10.dp))

                        order.items.forEach { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.product_name} (${item.qty} ${item.unit})",
                                    fontSize = 14.sp
                                )
                                Text(
                                    "₹${item.total}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Divider()
                        Spacer(Modifier.height(6.dp))

                        Text(
                            formatDate(order.created_at),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun StatusChip(status: String) {

    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFFA000)
        "delivered" -> Color(0xFF2E7D32)
        "cancelled" -> Color(0xFFC62828)
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
fun formatDate(raw: String): String {
    // Example: 2026-01-12T15:14:07.000000Z
    return try {
        raw.substring(0, 10) + " · " + raw.substring(11, 16)
    } catch (e: Exception) {
        raw
    }
}
@Composable
fun StatusBadge(status: String) {

    val (bg, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFFFF3CD) to Color(0xFF856404)
        "delivered" -> Color(0xFFE6F4EA) to Color(0xFF2E7D32)
        "cancelled" -> Color(0xFFFDECEA) to Color(0xFFC62828)
        else -> Color.LightGray to Color.DarkGray
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
