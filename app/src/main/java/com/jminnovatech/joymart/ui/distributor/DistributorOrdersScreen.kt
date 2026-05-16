

package com.jminnovatech.joymart.ui.distributor.orders

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jminnovatech.joymart.data.model.distributor.CustomerOrder
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun DistributorOrdersScreen(
    navController: NavHostController
) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var orders by remember {
        mutableStateOf<List<CustomerOrder>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }
    var selectedStatus by remember {
        mutableStateOf("pending")
    }
    var selectedOrder by remember {
        mutableStateOf<CustomerOrder?>(null)
    }

    suspend fun loadOrders() {

        loading = true

        try {

            val res = api.getCustomerOrders(selectedStatus)

            if (res.success) {
                orders = res.data
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        loading = false
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF7F9FC),
                        Color.White
                    )
                )
            )
    ) {

        Column {

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "pending" to "Pending",
                    "accepted" to "Accepted",
                    "rejected" to "Rejected",
                    "all" to "All"
                ).forEach { (value, label) ->

                    val selected = selectedStatus == value

                    FilterChip(

                        selected = selected,

                        onClick = {

                            selectedStatus = value

                            scope.launch {
                                loadOrders()
                            }
                        },

                        label = {
                            Text(label)
                        },

                        colors = FilterChipDefaults.filterChipColors(

                            selectedContainerColor =
                                when (value) {

                                    "pending" -> Color(0xFF1565C0)

                                    "accepted" -> Color(0xFF2E7D32)

                                    "rejected" -> Color.Red

                                    else -> Color.DarkGray
                                },

                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF7F9FC),
                                Color.White
                            )
                        )
                    )
            ) {


                if (loading) {

                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                } else {

                    if (orders.isEmpty()) {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                Icons.Default.People,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(70.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "No Pending Orders",
                                color = Color.Gray
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            items(orders) { order ->

                                Card(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .animateContentSize(),

                                    shape = RoundedCornerShape(22.dp),

                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 10.dp
                                    ),

                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {

                                    Column(
                                        modifier = Modifier.padding(18.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {

                                            Column {

                                                Text(
                                                    "Order #${order.id}",
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Spacer(Modifier.height(6.dp))

                                                Text(
                                                    "₹${order.total_amount}",
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor =
                                                        Color(0xFFFFC107)
                                                )
                                            ) {

                                                Text(
                                                    order.status.uppercase(),
                                                    modifier = Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 5.dp
                                                    ),
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(18.dp))
                                        when(order.status.lowercase()) {

                                            "pending" -> {

                                                Button(
                                                    onClick = {
                                                        selectedOrder = order
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF1565C0)
                                                    )
                                                ) {

                                                    Icon(
                                                        Icons.Default.ReceiptLong,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )

                                                    Spacer(Modifier.width(6.dp))

                                                    Text(
                                                        "Create Bill",
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            "accepted" -> {

                                                OutlinedButton(

                                                    onClick = {

                                                        if (
                                                            !order.bill_no.isNullOrEmpty()
                                                        ) {

                                                            val invoiceUrl =

                                                                RetrofitClient
                                                                    .getBaseUrl()
                                                                    .replace("/api/", "/invoice/") +

                                                                        order.bill_no

                                                            val intent = Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(invoiceUrl)
                                                            )



                                                            context.startActivity(intent)

                                                        } else {

                                                            Toast.makeText(
                                                                context,
                                                                "Bill not generated",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },

                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(46.dp),

                                                    shape = RoundedCornerShape(14.dp)
                                                ) {

                                                    Icon(
                                                        Icons.Default.Visibility,
                                                        contentDescription = null
                                                    )

                                                    Spacer(Modifier.width(6.dp))

                                                    Text("View Bill")
                                                }
                                            }

                                            "rejected" -> {

                                                OutlinedButton(

                                                    onClick = { },

                                                    enabled = false,

                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp),

                                                    shape = RoundedCornerShape(14.dp)
                                                ) {

                                                    Icon(
                                                        Icons.Default.Cancel,
                                                        contentDescription = null
                                                    )

                                                    Spacer(Modifier.width(6.dp))

                                                    Text("Rejected")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                selectedOrder?.let {

                    CustomerBillModal(
                        order = it,
                        onClose = {
                            selectedOrder = null
                        },
                        onSuccess = {
                            selectedOrder = null

                            scope.launch {
                                loadOrders()
                            }
                        }
                    )
                }
            }
        }
    }
}