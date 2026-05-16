package com.jminnovatech.joymart.ui.distributor.sales

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.jminnovatech.joymart.data.model.distributor.BillItem
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import java.text.DecimalFormat
import androidx.datastore.preferences.preferencesDataStore

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import com.jminnovatech.joymart.ui.distributor.offline.OfflineBillManager

val df2 = DecimalFormat("0.00")

fun format22(v:Double):String = df2.format(v)
fun isOnline(context: Context): Boolean {

    val cm =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val network =
        cm.activeNetwork ?: return false

    val capabilities =
        cm.getNetworkCapabilities(network)
            ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}




/* ---------------- MAIN SCREEN ----AC------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDistributorBillCreateScreen(nav: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf(listOf<ProductItem>()) }
    val cart = remember { mutableStateListOf<CartItem>() }

    var showProductDialog by remember { mutableStateOf(false) }

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerAddress by remember { mutableStateOf("") }

    var discountValue by remember { mutableStateOf("0") }
    var discountType by remember { mutableStateOf("amount") }
// amount or percent
    var gst by remember { mutableStateOf("0") }
    var paid by remember { mutableStateOf("0") }

    var loading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var offlineBills by remember {
        mutableStateOf(
            listOf<OfflineBillManager.OfflineBill>()
        )
    }
    var createdBillNo by remember { mutableStateOf("") }
    /* LOAD PRODUCTS */
    var saveStatus by remember {
        mutableStateOf("")
    }
    var showPendingDialog by remember {
        mutableStateOf(false)
    }
    var forceOffline by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(forceOffline) {

        try {

            if (!forceOffline && isOnline(context)) {

                val res =
                    RetrofitClient
                        .distributorApi
                        .getProducts()

                products = res.data?.data?.map {

                    ProductItem(
                        id = it.id,
                        title = it.title ?: "",
                        price = it.sell_price?.toDouble() ?: 0.0,
                        mrp = it.mrp?.toDouble() ?: 0.0,
                        unit = it.unit ?: "",
                        stock = it.stock_qty?.toDouble() ?: 0.0,
                        discountPercent =
                            it.discount_percent?.toDouble() ?: 0.0
                    )

                } ?: emptyList()

                OfflineBillManager.saveProducts(
                    context,
                    products
                )

            } else {

                products =
                    OfflineBillManager.getProducts(context)

                android.util.Log.d(
                    "OFFLINE_PRODUCTS",
                    "Loaded: ${products.size}"
                )

                if(products.isEmpty()){

                    Toast.makeText(
                        context,
                        "Connect internet once to load products",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } catch (_: Exception) {
            products =
                OfflineBillManager.getProducts(context)

            android.util.Log.d(
                "OFFLINE_PRODUCTS",
                "Catch Loaded: ${products.size}"
            )

            if(products.isEmpty()){

                Toast.makeText(
                    context,
                    "No cached products found",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        offlineBills =
            OfflineBillManager.getBills(context)

        if (!forceOffline && isOnline(context)) {

            OfflineBillManager.syncBills(context)

            offlineBills =
                OfflineBillManager.getBills(context)
        }
    }

    val subtotal = cart.sumOf { it.price * it.qty }

    val discountAmount =
        if(discountType == "percent")
            subtotal * (discountValue.toDoubleOrNull() ?: 0.0) / 100
        else
            discountValue.toDoubleOrNull() ?: 0.0

    val gstAmount =
        (subtotal - discountAmount) *
                (gst.toDoubleOrNull() ?: 0.0) / 100

    val total = subtotal - discountAmount + gstAmount

    val paidAmount = paid.toDoubleOrNull() ?: 0.0

    val due = total - paidAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "Create Bill",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(
                onClick = { nav.navigate("bill_history") }
            ) {
                Icon(Icons.Default.History, null)
            }
            BadgedBox(

                badge = {

                    val pendingCount =
                        offlineBills.count { !it.synced }

                    if (pendingCount > 0) {

                        Badge {
                            Text("$pendingCount")
                        }
                    }
                }

            ) {

                OutlinedButton(

                    onClick = {
                        showPendingDialog = true
                    }

                ) {

                    Text("Pending Bills")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    if(forceOffline)
                        Color(0xFFFFF3E0)
                    else
                        Color(0xFFE8F5E9)
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        if(forceOffline)
                            "Offline Billing Mode"
                        else
                            "Online Billing Mode",

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        if(forceOffline)
                            "Bills save instantly without internet"
                        else
                            "Bills directly upload to server",

                        color = Color.Gray,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = forceOffline,
                    onCheckedChange = {
                        forceOffline = it
                    }
                )
            }
        }
        /* CUSTOMER */

        OutlinedTextField(
            value = buyerName,
            onValueChange = { buyerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buyerPhone,
            onValueChange = {

                if (it.all { c -> c.isDigit() } && it.length <= 10)
                    buyerPhone = it
            },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buyerAddress,
            onValueChange = { buyerAddress = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        /* CHOOSE PRODUCT */



        Button(
            onClick = { showProductDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if(cart.isEmpty()) "Choose Products"
                else "Modify Products"
            )
        }

        Spacer(Modifier.height(12.dp))

        /* CART TABLE */

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .horizontalScroll(scroll)
                .border(1.dp, Color.LightGray)
        ) {

            Row(
                modifier = Modifier.background(Color(0xFF1976D2))
            ) {

                HeaderCell2("Product", 160.dp, Color.White)
                HeaderCell2("Qty", 80.dp, Color.White)
                HeaderCell2("MRP", 80.dp, Color.White)
                HeaderCell2("Sell", 80.dp, Color.White)
                HeaderCell2("Disc%", 80.dp, Color.White)
                HeaderCell2("Total", 100.dp, Color.White)
                HeaderCell2("Action", 70.dp, Color.White)
            }

            cart.forEachIndexed { index, item ->

                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.width(160.dp)) {

                        Text(item.title)

                        Text(
                            "Stock ${item.stock} ${item.unit}",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    TableCell(format2(item.qty), 80.dp)
                    TableCell("₹${format2(item.mrp)}", 80.dp)
                    TableCell("₹${format2(item.price)}", 80.dp)
                    TableCell("${format2(item.discountPercent)}%", 80.dp)
                    TableCell("₹${format2(item.price * item.qty)}", 100.dp)

                    IconButton(
                        onClick = { cart.removeAt(index) }
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }

                Divider()
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){

            OutlinedTextField(
                value = discountValue,
                onValueChange = { discountValue = it },
                label = {
                    Text(
                        if(discountType == "amount")
                            "Discount ₹"
                        else
                            "Discount %"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {

                    discountType =
                        if(discountType == "amount")
                            "percent"
                        else
                            "amount"
                }
            ){
                Text(
                    if(discountType == "amount")
                        "%"
                    else
                        "₹"
                )
            }

            OutlinedTextField(
                value = gst,
                onValueChange = { gst = it },
                label = { Text("GST %") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        /* TOTAL CARD */

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3F6FF)
            )
        ){

            Column(
                modifier = Modifier.padding(16.dp)
            ){

                SummaryRow2("Subtotal", subtotal)

                SummaryRow2(
                    if(discountType=="percent")
                        "Discount (${discountValue}%)"
                    else
                        "Discount",
                    discountAmount
                )

                SummaryRow2("GST", gstAmount)

                Divider()

                SummaryRow2(
                    "Total",
                    total,
                    true
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = paid,
            onValueChange = {

                val p = it.toDoubleOrNull()

                if (p != null && p <= total)
                    paid = it
            },
            label = { Text("Paid Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Text("Due : ₹${format2(due)}")

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !loading,
            onClick = {

                scope.launch {
                    try {
                    if (loading) return@launch
                    if (buyerName.isBlank()) {
                        Toast.makeText(context, "Enter customer name", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    if (buyerPhone.length != 10) {

                        Toast.makeText(
                            context,
                            "Enter valid 10 digit phone",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    if (cart.isEmpty()) {

                        Toast.makeText(
                            context,
                            "Add product first",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    loading = true

                    try {

                        val items = cart.map {

                            mapOf(
                                "id" to it.id,
                                "qty" to it.qty,
                                "price" to it.price
                            )
                        }

                        val json = Gson().toJson(items)

                        if (!forceOffline && isOnline(context))  {

                            try {

                                val res =
                                    RetrofitClient
                                        .distributorApi
                                        .createSale(
                                            buyerName,
                                            buyerPhone,
                                            buyerAddress,
                                            discountAmount,
                                            gstAmount,
                                            paidAmount,
                                            json
                                        )

                                createdBillNo =
                                    res.data.bill_no
                                saveStatus = "ONLINE"
                                showSuccess = true

                                loading = false
                                try {

                                    val refreshed =
                                        RetrofitClient
                                            .distributorApi
                                            .getProducts()

                                    products = refreshed.data?.data?.map {

                                        ProductItem(
                                            id = it.id,
                                            title = it.title ?: "",
                                            price = it.sell_price?.toDouble() ?: 0.0,
                                            mrp = it.mrp?.toDouble() ?: 0.0,
                                            unit = it.unit ?: "",
                                            stock = it.stock_qty?.toDouble() ?: 0.0,
                                            discountPercent =
                                                it.discount_percent?.toDouble() ?: 0.0
                                        )

                                    } ?: emptyList()

                                    OfflineBillManager.saveProducts(
                                        context,
                                        products
                                    )
                                    showProductDialog = false
                                } catch (_: Exception) {}
                            } catch (_: Exception) {

                                OfflineBillManager.saveOffline(
                                    context,
                                    OfflineBillManager.OfflineBill(

                                        localId =
                                            System.currentTimeMillis().toString(),

                                        buyerName = buyerName,
                                        buyerPhone = buyerPhone,
                                        buyerAddress = buyerAddress,

                                        discount = discountAmount,
                                        tax = gstAmount,
                                        paid = paidAmount,

                                        items = json,

                                        total = total
                                    )
                                )
                                saveStatus = "OFFLINE"
                                createdBillNo = "Offline Bill"
                                showSuccess = true
                                loading = false
                                Toast.makeText(
                                    context,
                                    "Saved Offline",
                                    Toast.LENGTH_LONG
                                ).show()
                                OfflineBillManager.reduceOfflineStock(
                                    context,
                                    cart
                                )
                                products =
                                    OfflineBillManager.getProducts(context)
                            }

                        } else {

                            OfflineBillManager.saveOffline(
                                context,
                                OfflineBillManager.OfflineBill(

                                    localId =
                                        System.currentTimeMillis().toString(),

                                    buyerName = buyerName,
                                    buyerPhone = buyerPhone,
                                    buyerAddress = buyerAddress,

                                    discount = discountAmount,
                                    tax = gstAmount,
                                    paid = paidAmount,

                                    items = json,

                                    total = total
                                )
                            )
                            OfflineBillManager.reduceOfflineStock(
                                context,
                                cart
                            )

                            products =
                                OfflineBillManager.getProducts(context)
                            saveStatus = "OFFLINE"
                            createdBillNo = "Offline Bill"
                            showSuccess = true
                            loading = false
                            Toast.makeText(
                                context,
                                "Offline bill saved successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        offlineBills =
                            OfflineBillManager.getBills(context)

                        buyerName = ""
                        buyerPhone = ""
                        buyerAddress = ""

                        discountValue = "0"
                        gst = "0"
                        paid = "0"

                        cart.clear()

                    } catch (_: Exception) {

                    }

                    loading = false
                    } catch (e: Exception) {

                        loading = false

                        Toast.makeText(
                            context,
                            e.message ?: "Something went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if (loading)
                CircularProgressIndicator()
            else
                Text(

                    if(forceOffline)
                        "Save Offline Bill"
                    else
                        "Create Bill"
                )
        }
        AnimatedVisibility(
            visible = saveStatus.isNotEmpty()
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),

                colors = CardDefaults.cardColors(

                    containerColor = when(saveStatus){

                        "ONLINE" ->
                            Color(0xFFE8F5E9)

                        "OFFLINE" ->
                            Color(0xFFFFF3E0)

                        else ->
                            Color(0xFFE3F2FD)
                    }
                )
            ) {

                Text(

                    when(saveStatus){

                        "ONLINE" ->
                            "🟢 Bill Saved Online successfully"

                        "OFFLINE" ->
                            "🟠 Offline bill saved successfully"

                        else ->
                            "🔵 Offline Bills Synced"
                    },

                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(20.dp))


    }

    if(showProductDialog){

        ProductSelectDialog(
            products = products,
            existingCart = cart,
            onClose = { showProductDialog = false },
            onConfirm = { selected ->

                cart.clear()
                cart.addAll(selected)

            }
        )
    }
    if (showSuccess) {

        Dialog(
            onDismissRequest = {
                showSuccess = false
            }
        ) {

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "✅",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(

                            if(saveStatus == "OFFLINE")
                                "Bill Saved Offline"
                            else
                                "Bill Created Successfully",

                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        createdBillNo,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(

                            containerColor = when(saveStatus){

                                "ONLINE" ->
                                    Color(0xFFE8F5E9)

                                "OFFLINE" ->
                                    Color(0xFFFFF3E0)

                                else ->
                                    Color(0xFFE3F2FD)
                            }
                        ),

                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(

                            when(saveStatus){

                                "ONLINE" ->
                                    "🟢 Bill Saved Online"

                                "OFFLINE" ->
                                    "🟠 Saved Offline"

                                else ->
                                    "🔵 Synced Later"
                            },

                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {

                            if(saveStatus == "OFFLINE") {

                                Toast.makeText(
                                    context,
                                    "Offline bill not uploaded yet",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://jminnovatech.xyz/enjoybazar/invoice/$createdBillNo"
                                    )
                                )

                                context.startActivity(intent)
                                showSuccess = false
                            }


                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("View Bill")
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            showSuccess = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Create Another Bill")
                    }
                }
            }
        }
    }
    if (showPendingDialog) {

        var syncing by remember {
            mutableStateOf(false)
        }

        Dialog(
            onDismissRequest = {
                if (!syncing)
                    showPendingDialog = false
            }
        ) {

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                "Offline Sync Center",
                                style =
                                    MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "${offlineBills.count { !it.synced }} pending bills",
                                color = Color.Gray
                            )
                        }

                        if (!syncing) {

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        showPendingDialog = false
                                    }
                                    .background(Color(0xFFF1F3F4))
                                    .padding(8.dp)
                            ) {
                                Text("✕")
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    val pendingBills =
                        offlineBills.filter { !it.synced }

                    if (pendingBills.isEmpty()) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                "✅",
                                style =
                                    MaterialTheme.typography.displayMedium
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "Everything Synced",
                                fontWeight = FontWeight.Bold,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "No pending offline bills",
                                color = Color.Gray
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier =
                                Modifier.heightIn(max = 450.dp)
                        ) {

                            items(pendingBills) { bill ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),

                                    shape =
                                        RoundedCornerShape(22.dp),

                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                Color(0xFFF8FAFF)
                                        ),

                                    border =
                                        BorderStroke(
                                            1.dp,
                                            Color(0xFFE3E8F0)
                                        )
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.padding(16.dp)
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier.fillMaxWidth(),

                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {

                                            Column {

                                                Text(
                                                    bill.buyerName,
                                                    fontWeight =
                                                        FontWeight.Bold,

                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .titleMedium
                                                )

                                                Spacer(
                                                    Modifier.height(4.dp)
                                                )

                                                Text(
                                                    bill.buyerPhone,
                                                    color = Color.Gray
                                                )
                                            }

                                            Card(
                                                shape =
                                                    RoundedCornerShape(50),

                                                colors =
                                                    CardDefaults.cardColors(
                                                        containerColor =
                                                            Color(0xFFFFF3E0)
                                                    )
                                            ) {

                                                Text(
                                                    "Pending",
                                                    color =
                                                        Color(0xFFF57C00),

                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 10.dp,
                                                            vertical = 5.dp
                                                        )
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(14.dp))

                                        Row(
                                            modifier =
                                                Modifier.fillMaxWidth(),

                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {

                                            Column {

                                                Text(
                                                    "Bill Amount",
                                                    color = Color.Gray
                                                )

                                                Text(
                                                    "₹${format2(bill.total)}",
                                                    fontWeight =
                                                        FontWeight.Bold,

                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .titleLarge
                                                )
                                            }

                                            Column(
                                                horizontalAlignment =
                                                    Alignment.End
                                            ) {

                                                Text(
                                                    "Saved Offline",
                                                    color = Color.Gray
                                                )

                                                Text(
                                                    java.text.SimpleDateFormat(
                                                        "dd MMM hh:mm a"
                                                    ).format(
                                                        java.util.Date(
                                                            bill.createdAt
                                                        )
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(16.dp))

                                        Row(
                                            horizontalArrangement =
                                                Arrangement.spacedBy(10.dp)
                                        ) {

                                            Button(

                                                enabled = !syncing,

                                                onClick = {

                                                    scope.launch {

                                                        syncing = true

                                                        val success =

                                                            OfflineBillManager
                                                                .syncSingleBill(
                                                                    context,
                                                                    bill
                                                                )

                                                        if(success){

                                                            offlineBills =
                                                                OfflineBillManager
                                                                    .getBills(context)

                                                            saveStatus =
                                                                "SYNCED"

                                                            try {

                                                                val refreshed =
                                                                    RetrofitClient
                                                                        .distributorApi
                                                                        .getProducts()

                                                                products =
                                                                    refreshed.data?.data?.map {

                                                                        ProductItem(
                                                                            id = it.id,
                                                                            title = it.title ?: "",
                                                                            price = it.sell_price?.toDouble() ?: 0.0,
                                                                            mrp = it.mrp?.toDouble() ?: 0.0,
                                                                            unit = it.unit ?: "",
                                                                            stock = it.stock_qty?.toDouble() ?: 0.0,
                                                                            discountPercent =
                                                                                it.discount_percent?.toDouble() ?: 0.0
                                                                        )

                                                                    } ?: emptyList()

                                                                OfflineBillManager
                                                                    .saveProducts(
                                                                        context,
                                                                        products
                                                                    )
                                                                showProductDialog = false
                                                            } catch (_: Exception) {}

                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Bill synced successfully",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                        } else {

                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Server unreachable",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        }

                                                        syncing = false
                                                    }
                                                },

                                                modifier =
                                                    Modifier.weight(1f),

                                                shape =
                                                    RoundedCornerShape(14.dp)
                                            ) {

                                                if(syncing){

                                                    CircularProgressIndicator(
                                                        strokeWidth = 2.dp,
                                                        modifier =
                                                            Modifier.size(18.dp)
                                                    )

                                                }else{

                                                    Text("Sync Now")
                                                }
                                            }

                                            OutlinedButton(

                                                enabled = !syncing,

                                                onClick = {

                                                    scope.launch {

                                                        OfflineBillManager
                                                            .restoreStock(
                                                                context,
                                                                bill
                                                            )

                                                        OfflineBillManager
                                                            .deleteBill(
                                                                context,
                                                                bill
                                                            )

                                                        offlineBills =
                                                            OfflineBillManager
                                                                .getBills(context)

                                                        products =
                                                            OfflineBillManager
                                                                .getProducts(context)

                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Offline bill removed",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }
                                                },

                                                modifier =
                                                    Modifier.weight(1f),

                                                shape =
                                                    RoundedCornerShape(14.dp)
                                            ) {

                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(

                            enabled = !syncing,

                            onClick = {

                                scope.launch {

                                    syncing = true

                                    val beforeCount =
                                        offlineBills.count { !it.synced }

                                    OfflineBillManager
                                        .syncBills(context)

                                    try {

                                        val refreshed =
                                            RetrofitClient
                                                .distributorApi
                                                .getProducts()

                                        products =
                                            refreshed.data?.data?.map {

                                                ProductItem(
                                                    id = it.id,
                                                    title = it.title ?: "",
                                                    price = it.sell_price?.toDouble() ?: 0.0,
                                                    mrp = it.mrp?.toDouble() ?: 0.0,
                                                    unit = it.unit ?: "",
                                                    stock = it.stock_qty?.toDouble() ?: 0.0,
                                                    discountPercent =
                                                        it.discount_percent?.toDouble() ?: 0.0
                                                )

                                            } ?: emptyList()
                                        showProductDialog = false
                                        OfflineBillManager
                                            .saveProducts(
                                                context,
                                                products
                                            )

                                    } catch (_: Exception) {}

                                    offlineBills =
                                        OfflineBillManager
                                            .getBills(context)

                                    val afterCount =
                                        offlineBills.count { !it.synced }

                                    syncing = false

                                    if(afterCount < beforeCount){

                                        saveStatus = "SYNCED"

                                        Toast
                                            .makeText(
                                                context,
                                                "All offline bills synced",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()

                                        if(afterCount == 0){

                                            showPendingDialog = false
                                        }

                                    } else {

                                        Toast
                                            .makeText(
                                                context,
                                                "Unable to connect server",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            if(syncing){

                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier =
                                        Modifier.size(22.dp)
                                )

                            }else{

                                Text(
                                    "Sync All Pending Bills",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -------- HISTORY -------- */



/* -------- HELPER UI -------- */

@Composable
fun HeaderCell2(text:String,width:Dp,color:Color){
    Text(
        text,
        modifier = Modifier.width(width),
        color = color,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun TableCell2(text:String,width:Dp){
    Text(
        text,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SummaryRow2(
    title:String,
    value:Double,
    isTotal:Boolean=false
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){

        Text(
            title,
            style = if(isTotal)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium
        )

        Text(
            "₹${format2(value)}",
            style = if(isTotal)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium
        )
    }
}

