package com.jminnovatech.joymart.ui.distributor.modal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModal(
    saleId: Int,
    maxAmount: Double,
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("cash") }
    var needApproval by remember { mutableStateOf(true) }
    var utr by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!loading) onClose() }) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp
        ) {

            Column(Modifier.padding(20.dp)) {

                Text("💳 Add Payment", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        val v = it.toDoubleOrNull()
                        if (v == null || v <= maxAmount) amount = it
                    },
                    label = { Text("Amount (Max ₹$maxAmount)") }
                )

                Spacer(Modifier.height(10.dp))

                // Method
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Method") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        listOf("cash","upi","bank").forEach {
                            DropdownMenuItem(
                                text = { Text(it.uppercase()) },
                                onClick = {
                                    method = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Approval
                if (method != "cash") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = needApproval,
                            onCheckedChange = { needApproval = it }
                        )
                        Text("Need Approval")
                    }
                }

                // UTR
                if (needApproval && method != "cash") {
                    OutlinedTextField(
                        value = utr,
                        onValueChange = { utr = it },
                        label = { Text("UTR / Transaction ID") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val amt = amount.toDoubleOrNull()

                        if (amt == null || amt <= 0) return@Button

                        scope.launch {
                            try {
                                loading = true

                                api.submitPayment(
                                    saleId,
                                    amt,
                                    method.lowercase(),
                                    if (method != "cash" && needApproval) 1 else 0,
                                    if (utr.isBlank()) null else utr
                                )

                                loading = false
                                onSuccess()
                                onClose()

                            } catch (e: Exception) {
                                loading = false
                            }
                        }
                    }
                ) {
                    Text(if (loading) "Processing..." else "Submit Payment")
                }
            }
        }
    }
}