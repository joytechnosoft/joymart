package com.jminnovatech.joymart.ui.distributor
import com.jminnovatech.joymart.data.model.distributor.BillPaymentItem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

// ==============================
// DATA MODELS
// ==============================


// ==============================
// MAIN SCREEN
// ==============================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorBillPaymentScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var bills by remember {
        mutableStateOf<List<BillPaymentItem>>(emptyList())
    }
    var totalDue by remember { mutableStateOf(0.0) }

    var loading by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    var search by remember { mutableStateOf("") }

    var selectedBill by remember {
        mutableStateOf<BillPaymentItem?>(null)
    }

    suspend fun loadBills() {

        try {

            val response = api.getBillPayments()

            if (response.success) {

                bills = response.data?.data ?: emptyList()

                totalDue = response.total_due
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        loading = false
        refreshing = false
    }

    LaunchedEffect(Unit) {
        loadBills()
    }

    val filteredBills = bills.filter {

        it.bill_no.contains(search, true) ||
                it.buyer_name.contains(search, true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF6F9FF),
                        Color(0xFFE8F0FF)
                    )
                )
            )
    ) {

        Column {

            // =========================
            // TOP CARD
            // =========================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Total Due",
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "₹${"%.2f".format(totalDue)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // =========================
            // SEARCH BAR
            // =========================

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, null)
                },
                trailingIcon = {

                    IconButton(
                        onClick = {
                            scope.launch {
                                refreshing = true
                                loadBills()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            null
                        )
                    }
                },
                placeholder = {
                    Text("Search bill / customer")
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            // =========================
            // BILL LIST
            // =========================

            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

                items(filteredBills) { bill ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        bill.bill_no,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1)
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        bill.buyer_name,
                                        color = Color.Gray
                                    )
                                }

                                Surface(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(50)
                                ) {

                                    Text(
                                        "₹${bill.due_amount}",
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        "Total",
                                        color = Color.Gray
                                    )

                                    Text(
                                        "₹${bill.total}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row {

                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2E7D32)
                                        ),
                                        shape = RoundedCornerShape(50),
                                        onClick = {
                                            selectedBill = bill
                                        }
                                    ) {

                                        Icon(
                                            Icons.Default.Payment,
                                            null
                                        )

                                        Spacer(Modifier.width(6.dp))

                                        Text("Receive")
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    OutlinedButton(
                                        shape = RoundedCornerShape(50),
                                        onClick = {

                                        }
                                    ) {

                                        Icon(
                                            Icons.Default.Receipt,
                                            null
                                        )

                                        Spacer(Modifier.width(6.dp))

                                        Text("View")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================
        // LOADER
        // =========================

        AnimatedVisibility(
            visible = loading || refreshing,
            modifier = Modifier.align(Alignment.Center)
        ) {

            Card(
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator()

                    Spacer(Modifier.height(12.dp))

                    Text("Loading...")
                }
            }
        }
    }

    // =========================
    // PAYMENT MODAL
    // =========================

    selectedBill?.let { bill ->

        PaymentModal(
            bill = bill,
            onClose = {
                selectedBill = null
            },
            onSuccess = {

                scope.launch {
                    loadBills()
                }
            }
        )
    }

}

// ==============================
// PAYMENT MODAL
// ==============================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModal(
    bill: BillPaymentItem,
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }

    var method by remember {
        mutableStateOf("cash")
    }

    var needApproval by remember {
        mutableStateOf(true)
    }

    var utr by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(false)
    }
    var showSuccess by remember {
        mutableStateOf(false)
    }
    var expanded by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = {
            if (!loading) onClose()
        }
    ) {

        Card(
            shape = RoundedCornerShape(28.dp)
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                // HEADER

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            "Receive Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            bill.bill_no,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!loading) onClose()
                        }
                    ) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // CUSTOMER

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            bill.buyer_name,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Due Amount",
                            color = Color.Gray
                        )

                        Text(
                            "₹${bill.due_amount}",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // AMOUNT

                OutlinedTextField(
                    value = amount,
                    onValueChange = {

                        val v = it.toDoubleOrNull()

                        if (v == null || v <= bill.due_amount) {
                            amount = it
                        }
                    },
                    label = {
                        Text("Amount")
                    },
                    placeholder = {
                        Text("Max ₹${bill.due_amount}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(14.dp))

                // METHOD

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {

                    OutlinedTextField(
                        value = method.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Payment Method")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        listOf(
                            "cash",
                            "upi",
                            "bank"
                        ).forEach {

                            DropdownMenuItem(
                                text = {
                                    Text(it.uppercase())
                                },
                                onClick = {
                                    method = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // APPROVAL

                if (method != "cash") {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = needApproval,
                            onCheckedChange = {
                                needApproval = it
                            }
                        )

                        Text("Need Approval")
                    }
                }

                // UTR

                AnimatedVisibility(
                    visible = method != "cash" && needApproval
                ) {

                    OutlinedTextField(
                        value = utr,
                        onValueChange = {
                            utr = it
                        },
                        label = {
                            Text("UTR / Transaction ID")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                // BUTTON

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    onClick = {

                        val payAmount =
                            amount.toDoubleOrNull()

                        if (payAmount == null || payAmount <= 0) {
                            return@Button
                        }

                        scope.launch {

                            try {

                                loading = true

                                api.submitPayment(
                                    saleId = bill.id,
                                    amount = payAmount,
                                    method = method.lowercase(), // ✅ IMPORTANT
                                    needApproval =
                                        if (method != "cash" && needApproval) 1 else 0,
                                    utrNo =
                                        if (method == "cash") null
                                        else utr.takeIf { it.isNotBlank() }
                                )

                                loading = false

                                showSuccess = true

                                onSuccess()

                            } catch (e: Exception) {

                                loading = false

                                e.printStackTrace()
                            }
                        }
                    }
                ) {

                    if (loading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                    } else {

                        Icon(
                            Icons.Default.Payment,
                            null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Submit Payment")
                    }
                }
            }
        }
    }
    if (showSuccess) {

        AlertDialog(

            onDismissRequest = {

                showSuccess = false
                onClose()
            },

            confirmButton = {

                Button(
                    onClick = {

                        showSuccess = false
                        onClose()
                    }
                ) {

                    Text("OK")
                }
            },

            title = {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Payment Success")
                }
            },

            text = {

                Text(
                    if (method == "cash")
                        "Cash payment received successfully."
                    else
                        "Payment submitted successfully."
                )
            }
        )
    }
}