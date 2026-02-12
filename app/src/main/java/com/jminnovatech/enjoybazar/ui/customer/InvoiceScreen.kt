package com.jminnovatech.joymart.ui.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jminnovatech.data.model.customer.CustomerCartItem

@Composable
fun InvoiceScreen(
    cart: List<CustomerCartItem>,
    onConfirm: () -> Unit
) {
    val subtotal = cart.sumOf { it.product.sell_price * it.qty }
    val mrpTotal = cart.sumOf { (it.product.sell_price * 1.15) * it.qty }
    val discount = mrpTotal - subtotal

    Column(Modifier.padding(16.dp)) {

        Text("🧾 Invoice", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        cart.forEach {
            Text("${it.product.title} × ${it.qty} = ₹${it.product.sell_price * it.qty}")
        }

        Divider(Modifier.padding(vertical = 12.dp))

        Row { Text("MRP"); Spacer(Modifier.weight(1f)); Text("₹$mrpTotal") }
        Row { Text("Discount"); Spacer(Modifier.weight(1f)); Text("-₹$discount") }
        Row {
            Text("Total", fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("₹$subtotal", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onConfirm
        ) {
            Text("Confirm Order")
        }
    }
}
