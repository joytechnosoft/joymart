package com.jminnovatech.joymart.ui.distributor.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jminnovatech.joymart.data.model.distributor.CustomerOrder
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun CustomerBillModal(
    order: CustomerOrder,
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var paidAmount by remember {
        mutableStateOf("")
    }

    val total = order.total_amount

    val paid = paidAmount.toDoubleOrNull() ?: 0.0

    val due = total - paid

    val paymentStatus = when {

        paid >= total -> "Paid"

        paid > 0 -> "Partial"

        else -> "Due"
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var successDialog by remember {
        mutableStateOf(false)
    }
    var errorDialog by remember {
        mutableStateOf(false)
    }

    var rejectDialog by remember {
        mutableStateOf(false)
    }

    var invoiceUrl by remember {
        mutableStateOf("")
    }

    Dialog(
        onDismissRequest = {
            if (!loading) onClose()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {

            Box {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {

                    item {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                "Create Bill",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = onClose
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }

                    item {

                        OutlinedTextField(
                            value = "Moumita",
                            onValueChange = {},
                            enabled = false,
                            label = {
                                Text("Customer Name")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "9093931918",
                            onValueChange = {},
                            enabled = false,
                            label = {
                                Text("Phone")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "Kolkata",
                            onValueChange = {},
                            enabled = false,
                            label = {
                                Text("Address")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))
                    }

                    items(order.items.size) { index ->

                        val item = order.items[index]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    item.product_name,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Text("MRP ₹${item.mrp}")

                                    Text("Sell ₹${item.price}")
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Text("Qty ${item.qty}")

                                    Text(
                                        "₹${item.total}",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFFF5F7FB)
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {

                                Text(
                                    "Payment Section",
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = paidAmount,
                                    onValueChange = {
                                        paidAmount = it
                                    },
                                    label = {
                                        Text("Paid Amount")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "Total : ₹$total",
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Due : ₹$due",
                                    color = Color.Red
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Status : $paymentStatus",
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        onClick = {
                            rejectDialog = true
                        }
                    ) {
                        Text("Reject")
                    }

                    Button(
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        onClick = {

                            scope.launch {

                                loading = true

                                try {

                                    val res =
                                        api.acceptCustomerOrder(
                                            order.id,
                                            paid
                                        )

                                    val billNo = res.bill_no

                                    println("Generated Bill No = $billNo")

                                    if (!billNo.isNullOrEmpty()) {

                                        invoiceUrl =
                                            "https://jminnovatech.xyz/invoice/$billNo"

                                        successDialog = true

                                    } else {

                                        errorDialog = true
                                    }

                                } catch (e: Exception) {

                                    e.printStackTrace()

                                    errorDialog = true
                                }

                                loading = false
                            }
                        }
                    ) {

                        Text("Accept & Generate Bill")
                    }
                }

                if (loading) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (successDialog) {

        AlertDialog(
            onDismissRequest = {},

            confirmButton = {

                TextButton(
                    onClick = {

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(invoiceUrl)
                        )

                        context.startActivity(intent)

                        successDialog = false

                        onSuccess()
                    }
                ) {
                    Text("View Invoice")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        successDialog = false

                        onSuccess()
                    }
                ) {
                    Text("Close")
                }
            },

            icon = {
                Icon(
                    Icons.Default.Done,
                    null,
                    tint = Color(0xFF2E7D32)
                )
            },

            title = {
                Text("Success")
            },

            text = {
                Text("Bill Generated Successfully")
            }
        )
    }

    if (rejectDialog) {

        AlertDialog(

            onDismissRequest = {
                rejectDialog = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        scope.launch {

                            loading = true

                            try {

                                api.rejectCustomerOrder(order.id)

                                onSuccess()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            loading = false
                        }
                    }
                ) {
                    Text("Reject")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        rejectDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },

            icon = {
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint = Color.Red
                )
            },

            title = {
                Text("Reject Order")
            },

            text = {
                Text("Are you sure?")
            }
        )
    }
    if (errorDialog) {

        AlertDialog(

            onDismissRequest = {
                errorDialog = false
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        errorDialog = false
                    }
                ) {
                    Text("OK")
                }
            },

            title = {
                Text("Invoice Error")
            },

            text = {
                Text("Bill number missing from API")
            }
        )
    }
}