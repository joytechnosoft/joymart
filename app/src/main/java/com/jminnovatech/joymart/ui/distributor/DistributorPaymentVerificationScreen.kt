package com.jminnovatech.joymart.ui.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jminnovatech.joymart.data.model.distributor.PendingPaymentItem
import com.jminnovatech.joymart.data.model.distributor.PaymentHistoryItem
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun DistributorPaymentVerificationScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var payments by remember {
        mutableStateOf<List<PendingPaymentItem>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var rejectId by remember {
        mutableStateOf<Int?>(null)
    }

    var rejectReason by remember {
        mutableStateOf("")
    }

    var history by remember {
        mutableStateOf<List<PaymentHistoryItem>>(emptyList())
    }

    var showHistory by remember {
        mutableStateOf(false)
    }
    var actionLoading by remember {
        mutableStateOf(false)
    }

    var successMessage by remember {
        mutableStateOf<String?>(null)
    }

    var approveId by remember {
        mutableStateOf<Int?>(null)
    }

    suspend fun loadData() {

        try {

            val res = api.getPendingPayments()

            if (res.success) {
                payments = res.data
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        loading = false
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF5F9FF),
                        Color(0xFFEAF2FF)
                    )
                )
            )
    ) {

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    "Payment Verification",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if(
                !loading &&
                payments.isEmpty()
            ) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(60.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "No Pending Payments",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "All payment requests are verified",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            items(payments) { p ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f),
                    shape = RoundedCornerShape(24.dp)
                ){

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
                                    p.bill_no,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    p.buyer_name,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                "₹${p.amount}",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF1F5F9)
                        ) {

                            Text(
                                p.utr_no,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row {

                            Button(
                                onClick = {

                                    scope.launch {

                                        approveId = p.id

                                        payments =
                                            payments.filter {
                                                it.id != p.id
                                            }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {

                                Icon(
                                    Icons.Default.Verified,
                                    null
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Approve")
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    rejectId = p.id
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {

                                Text("Reject")
                            }

                            Spacer(Modifier.width(8.dp))

                            OutlinedButton(

                                onClick = {

                                    scope.launch {

                                        actionLoading = true

                                        try {

                                            val res =
                                                api.paymentHistory(
                                                    p.sale_id
                                                )

                                            history = res.data
                                            showHistory = true

                                        } catch (e: Exception) {

                                            history = emptyList()
                                            showHistory = true

                                        }

                                        actionLoading = false
                                    }
                                },

                                shape = RoundedCornerShape(50),

                                modifier = Modifier.height(42.dp)

                            ) {

                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp)
                                )

                                Spacer(Modifier.width(4.dp))

//                                Text("History")
                            }
                        }
                    }
                }
            }
        }

        if (loading) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }
        if (actionLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
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

                        Text("Please wait...")
                    }
                }
            }
        }
        successMessage?.let { msg ->

            AlertDialog(

                onDismissRequest = {

                    approveId = null

                    scope.launch {
                        loadData()
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {
                            successMessage = null
                        }
                    ) {
                        Text("OK")
                    }
                },

                title = {
                    Text("Success")
                },

                text = {
                    Text(msg)
                }
            )
        }
        approveId?.let { id ->

            AlertDialog(

                onDismissRequest = {
                    approveId = null
                },

                confirmButton = {

                    Button(

                        onClick = {

                            scope.launch {

                                actionLoading = true

                                try {

                                    api.approvePayment(id)

                                    payments =
                                        payments.filter {
                                            it.id != id
                                        }

                                    successMessage =
                                        "Payment Approved Successfully"

                                } catch (e: Exception) {

                                    successMessage =
                                        "Approval Failed"
                                }

                                actionLoading = false
                                approveId = null
                            }
                        }
                    ) {

                        Text("Approve")
                    }
                },

                dismissButton = {

                    OutlinedButton(

                        onClick = {

                            approveId = null

                            scope.launch {
                                loadData()
                            }
                        }
                    ) {
                        Text("Cancel")
                    }
                },

                title = {
                    Text("Approve Payment")
                },

                text = {
                    Text("Are you sure you want to approve this payment?")
                }
            )
        }
    }

    // =========================
    // REJECT MODAL
    // =========================

    rejectId?.let { id ->

        Dialog(
            onDismissRequest = {

                rejectId = null
                rejectReason = ""

                scope.launch {
                    loadData()
                }
            }
        ) {

            Card(
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        "Reject Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = {
                            rejectReason = it
                        },
                        label = {
                            Text("Reason")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        onClick = {

                            scope.launch {

                                api.rejectPayment(
                                    id,
                                    rejectReason
                                )

                                payments =
                                    payments.filter {
                                        it.id != id
                                    }

                                successMessage =
                                    "Payment Rejected Successfully"

                                rejectId = null
                                rejectReason = ""
                            }
                        }
                    ) {

                        Text("Confirm Reject")
                    }
                }
            }
        }
    }

    // =========================
    // HISTORY MODAL
    // =========================

    if (showHistory) {

        Dialog(
            onDismissRequest = {
                showHistory = false
            }
        ) {

            Card(
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            "Payment History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                showHistory = false
                            }
                        ) {

                            Icon(
                                Icons.Default.Close,
                                null
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    if(history.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                "No Payment History Available",
                                color = Color.Gray
                            )
                        }

                    } else {
                    LazyColumn {

                        items(history) { h ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {

                                    Text(
                                        "₹${h.amount}",
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(h.method)

                                    Text(
                                        h.status.uppercase(),
                                        color =
                                            when (h.status) {

                                                "approved" ->
                                                    Color(0xFF2E7D32)

                                                "rejected" ->
                                                    Color.Red

                                                else ->
                                                    Color(0xFFFF9800)
                                            }
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}