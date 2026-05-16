package com.jminnovatech.joymart.ui.distributor.sales

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

val df = DecimalFormat("0.00")

fun format2(v:Double):String = df.format(v)

/* ---------------- MODELS ---------------- */

data class ProductItem(
    val id:Int,
    val title:String,
    val price:Double,
    val mrp:Double,
    val unit:String,
    val stock:Double,
    val discountPercent:Double
)

data class CartItem(
    val id:Int,
    val title:String,
    val price:Double,
    val mrp:Double,
    val unit:String,
    val discountPercent:Double,
    val stock:Double,
    var qty:Double
)

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorBillCreateScreen(nav: NavController) {

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
    var createdBillNo by remember { mutableStateOf("") }
    /* LOAD PRODUCTS */

    LaunchedEffect(Unit) {

        try {

            val res = RetrofitClient.distributorApi.getProducts()

            products = res.data?.data?.map {

                ProductItem(
                    id = it.id,
                    title = it.title ?: "",
                    price = it.sell_price?.toDouble() ?: 0.0,
                    mrp = it.mrp?.toDouble() ?: 0.0,
                    unit = it.unit ?: "",
                    stock = it.stock_qty?.toDouble() ?: 0.0,
                    discountPercent = it.discount_percent?.toDouble() ?: 0.0
                )

            } ?: emptyList()

        } catch (_: Exception) {}
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
        }

        Spacer(Modifier.height(12.dp))

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

                HeaderCell("Product", 160.dp, Color.White)
                HeaderCell("Qty", 80.dp, Color.White)
                HeaderCell("MRP", 80.dp, Color.White)
                HeaderCell("Sell", 80.dp, Color.White)
                HeaderCell("Disc%", 80.dp, Color.White)
                HeaderCell("Total", 100.dp, Color.White)
                HeaderCell("Action", 70.dp, Color.White)
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

                SummaryRow("Subtotal", subtotal)

                SummaryRow(
                    if(discountType=="percent")
                        "Discount (${discountValue}%)"
                    else
                        "Discount",
                    discountAmount
                )

                SummaryRow("GST", gstAmount)

                Divider()

                SummaryRow(
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
            onClick = {

                scope.launch {

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

                        val res =
                            RetrofitClient.distributorApi.createSale(
                                buyerName,
                                buyerPhone,
                                buyerAddress,
                                discountAmount,
                                gstAmount,
                                paidAmount,
                                json
                            )



                        val billNo = res.data.bill_no

                        createdBillNo = billNo

// ✅ CLEAR ALL FORM
                        buyerName = ""
                        buyerPhone = ""
                        buyerAddress = ""

                        discountValue = "0"
                        gst = "0"
                        paid = "0"

                        cart.clear()

// ✅ SHOW SUCCESS UI
                        showSuccess = true


                    } catch (_: Exception) {}

                    loading = false
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if (loading)
                CircularProgressIndicator()
            else
                Text("Save Bill")
        }
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
                        "Bill Created Successfully",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        createdBillNo,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://jminnovatech.xyz/enjoybazar/invoice/$createdBillNo"
                                )
                            )

                            context.startActivity(intent)

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
}

/* -------- HISTORY -------- */

@Composable
fun DistributorBillHistoryScreen(){


    var bills by remember { mutableStateOf(listOf<BillItem>()) }

    LaunchedEffect(Unit){

        val res = RetrofitClient
            .distributorApi
            .getSales()

        bills = res.data?.data ?: emptyList()
    }

    LazyColumn{

        items(bills){bill->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ){

                Column(
                    modifier = Modifier.padding(12.dp)
                ){

                    Text("Bill : ${bill.bill_no}")
                    Text("Customer : ${bill.buyer_name}")
                    Text("Total : ₹${bill.total}")
                }
            }
        }
    }


}

/* -------- HELPER UI -------- */

@Composable
fun HeaderCell(text:String,width:Dp,color:Color){
    Text(
        text,
        modifier = Modifier.width(width),
        color = color,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun TableCell(text:String,width:Dp){
    Text(
        text,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SummaryRow(
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

@Composable
fun ProductSelectDialog(
    products: List<ProductItem>,
    existingCart: List<CartItem>,
    onConfirm: (List<CartItem>) -> Unit,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    var search by remember { mutableStateOf("") }

    val selected =
        remember {
            mutableStateListOf<CartItem>().apply {
                addAll(existingCart)
            }
        }

    val filtered =
        if (search.isBlank()) products
        else products.filter { it.title.contains(search, true) }

    Dialog(onDismissRequest = onClose) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF4F6FF)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                /* HEADER */

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        "Select Products",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        "✕",
                        modifier = Modifier.clickable { onClose() },
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(Modifier.height(10.dp))

                /* SEARCH */

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search Product") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(12.dp))

                /* PRODUCT LIST */

                Text(
                    "Available Products",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(filtered) { p ->

                        val outOfStock = p.stock <= 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (outOfStock)
                                        Color(0xFFFFEBEE)
                                    else
                                        Color.White
                                )
                                .border(
                                    1.dp,
                                    Color(0xFFE3E8F0),
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable(enabled = !outOfStock) {

                                    if (selected.none { it.id == p.id }) {

                                        selected.add(
                                            0,
                                            CartItem(
                                                p.id,
                                                p.title,
                                                p.price,
                                                p.mrp,
                                                p.unit,
                                                p.discountPercent,
                                                p.stock,
                                                1.0
                                            )
                                        )
                                    }
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    p.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    if (outOfStock)
                                        "Out of Stock"
                                    else
                                        "Stock ${p.stock} ${p.unit}",
                                    color =
                                        if (outOfStock)
                                            Color.Red
                                        else
                                            Color(0xFF2E7D32),
                                    style =
                                        MaterialTheme.typography.labelMedium
                                )

                                Spacer(Modifier.height(4.dp))

                                Row {

                                    Text(
                                        "MRP ₹${p.mrp}",
                                        style =
                                            MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    if (p.discountPercent > 0) {

                                        Text(
                                            "${format2(p.discountPercent)}% OFF",
                                            color = Color(0xFF1B5E20),
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                "₹${p.price}",
                                color =
                                    if (outOfStock)
                                        Color.Gray
                                    else
                                        Color(0xFF2962FF),
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                /* SELECTED PRODUCTS */

                Text(
                    "Selected Products",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(selected) { item ->

                        var qty by remember {
                            mutableStateOf(item.qty)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(
                                    1.dp,
                                    Color(0xFFE3E8F0),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    item.title,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    "Stock ${item.stock} ${item.unit}",
                                    color = Color.Gray,
                                    style =
                                        MaterialTheme.typography.labelSmall
                                )
                            }

                            /* QTY CONTROLLER */

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                modifier = Modifier
                                    .border(
                                        2.dp,
                                        Color(0xFF4A90E2),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE3F2FD))
                                        .clickable {

                                            val step =
                                                if (item.unit.lowercase()
                                                    in listOf("kg", "ltr", "gram"))
                                                    0.100
                                                else
                                                    1.0

                                            val newQty = qty - step

                                            if (newQty <= 0) {

                                                selected.remove(item)

                                            } else {

                                                qty = newQty

                                                val index =
                                                    selected.indexOf(item)

                                                selected[index] =
                                                    selected[index].copy(qty = newQty)
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        "-",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedTextField(
                                    value = format2(qty),
                                    onValueChange = {

                                        val v =
                                            it.toDoubleOrNull()

                                        if (v != null) {

                                            if (v > item.stock) {

                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Stock only ${item.stock}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()

                                            } else {

                                                qty = v

                                                val index =
                                                    selected.indexOf(item)

                                                selected[index] =
                                                    selected[index].copy(qty = v)
                                            }
                                        }
                                    },
                                    keyboardOptions =
                                        KeyboardOptions(
                                            keyboardType =
                                                KeyboardType.Decimal
                                        ),
                                    singleLine = true,
                                    modifier =
                                        Modifier.width(80.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE3F2FD))
                                        .clickable {

                                            val step =
                                                if (item.unit.lowercase()
                                                    in listOf("kg", "ltr", "gram"))
                                                    0.100
                                                else
                                                    1.0

                                            val newQty = qty + step

                                            if (newQty > item.stock) {

                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Stock only ${item.stock}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()

                                            } else {

                                                qty = newQty

                                                val index =
                                                    selected.indexOf(item)

                                                selected[index] =
                                                    selected[index].copy(qty = newQty)
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        "+",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {

                        onConfirm(selected)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2962FF)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {

                    Text(
                        "Update Products",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}