package com.jminnovatech.joymart.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jminnovatech.data.model.customer.CustomerCartItem

@Composable
fun CartBill(cart: List<CustomerCartItem>) {

    val subtotal = cart.sumOf { it.product.sell_price * it.qty }
    val delivery = 0.0
    val total = subtotal + delivery

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row {
                Text("Subtotal")
                Spacer(Modifier.weight(1f))
                Text("₹$subtotal")
            }

            Row {
                Text("Delivery")
                Spacer(Modifier.weight(1f))
                Text("₹$delivery")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Row {
                Text("Total", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("₹$total", fontWeight = FontWeight.Bold)
            }
        }
    }
}
