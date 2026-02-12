package com.jminnovatech.joymart.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jminnovatech.joymart.ui.customer.vm.CustomerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun CustomerCart(vm: CustomerViewModel) {

    val cart by vm.cart.collectAsState()
    val profile by vm.profile.collectAsState()
    val orderSuccess by vm.orderSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val total = cart.sumOf {
        it.product.sell_price * it.qty
    }

    LaunchedEffect(Unit) {
        vm.loadProfile()
    }

    // ✅ SHOW SNACKBAR ON SUCCESS
    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            snackbarHostState.showSnackbar("✅ Order placed successfully")
            vm.clearOrderSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                "🛒 My Cart",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            if (cart.isEmpty()) {
                Text("Your cart is empty")
                return@Column
            }

            cart.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(item.product.title, fontWeight = FontWeight.Bold)
                        Text("₹ ${item.product.sell_price} × ${item.qty}")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 BILL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    BillRow("Subtotal", total)
                    BillRow("Delivery", 0.0)

                    Divider(Modifier.padding(vertical = 6.dp))

                    BillRow("Total", total, bold = true)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    vm.placeOrder(
                        buyerName = profile?.name ?: "Customer",
                        buyerPhone = profile?.phone ?: "",
                        buyerAddress = profile?.address ?: ""
                    )
                }
            ) {
                Text("Place Order")
            }
        }
    }
}


@Composable
fun BillRow(
    label: String,
    amount: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            "₹ $amount",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

