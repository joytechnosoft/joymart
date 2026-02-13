package com.jminnovatech.joymart.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jminnovatech.joymart.data.model.customer.CustomerProduct

@Composable
fun ProductCard(
    product: CustomerProduct,
    qty: Double,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = "https://jminnovatech.xyz/${product.image}",
                contentDescription = product.title,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                // 🔹 TITLE
                Text(
                    product.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                // 🔹 DESCRIPTION (ONLY IF DB HAS IT)
                product.description?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(Modifier.height(6.dp))

                // 💰 PRICE ROW (REAL DATA)
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        "₹ ${product.sell_price}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // 🔹 MRP (REAL)
                    product.mrp?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "₹ $it",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 🔹 DISCOUNT (REAL)
                    product.discount_percent?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "$it% OFF",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    "Stock: ${product.stock_qty} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))

                // ➕➖ ADD / REMOVE (UNCHANGED)
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = onRemove,
                        enabled = qty > 0
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove")
                    }

                    Text(
                        qty.toInt().toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        }
    }
}
