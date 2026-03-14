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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
fun DistributorBillCreateScreen(nav:NavController){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf(listOf<ProductItem>()) }
    val cart = remember { mutableStateListOf<CartItem>() }

    var search by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerAddress by remember { mutableStateOf("") }

    var discountValue by remember { mutableStateOf("0") }
    var gst by remember { mutableStateOf("0") }
    var paid by remember { mutableStateOf("0") }

    var loading by remember { mutableStateOf(false) }

    /* -------- LOAD PRODUCTS -------- */

    LaunchedEffect(Unit){

        try{

            val res = RetrofitClient.distributorApi.getProducts()

            products = res.data?.data?.map{

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

        }catch(_:Exception){}
    }

    /* -------- CALCULATIONS -------- */

    val subtotal = cart.sumOf { it.price * it.qty }

    val discountAmount = discountValue.toDoubleOrNull() ?: 0.0

    val gstAmount =
        (subtotal-discountAmount) *
                (gst.toDoubleOrNull() ?: 0.0) / 100

    val total = subtotal-discountAmount+gstAmount

    val paidAmount = paid.toDoubleOrNull() ?: 0.0

    val due = total-paidAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){

        /* -------- HEADER -------- */

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){

            Text(
                "Create Bill",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(
                onClick={ nav.navigate("bill_history") }
            ){
                Icon(Icons.Default.History,null)
            }
        }

        Spacer(Modifier.height(12.dp))

        /* -------- CUSTOMER -------- */

        OutlinedTextField(
            value = buyerName,
            onValueChange = { buyerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buyerPhone,
            onValueChange = { newValue ->

                if(newValue.all { it.isDigit() } && newValue.length <= 10){
                    buyerPhone = newValue
                }
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

        Spacer(Modifier.height(10.dp))

        /* -------- PRODUCT SEARCH -------- */

        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it
                expanded = true
            },
            label = { Text("Search Product") },
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ){

            DropdownMenu(
                expanded = expanded && search.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ){

                products
                    .filter { it.title.contains(search,true) }
                    .take(8)
                    .forEach{p->

                        DropdownMenuItem(
                            text = {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){

                                    Column {

                                        Text(
                                            p.title,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            "Stock ${p.stock} ${p.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Column {

                                        Text(
                                            "₹${p.price}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            "${p.discountPercent}% off",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                            },
                            onClick={

                                expanded=false
                                search=""

                                if(p.stock<=0){

                                    Toast.makeText(
                                        context,
                                        "Out of stock",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    return@DropdownMenuItem
                                }

                                if(cart.any{it.id==p.id}){

                                    Toast.makeText(
                                        context,
                                        "Already added",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    return@DropdownMenuItem
                                }

                                cart.add(
                                    CartItem(
                                        p.id,p.title,p.price,p.mrp,
                                        p.unit,p.discountPercent,p.stock,1.0
                                    )
                                )
                            }
                        )
                    }
            }
        }

        Spacer(Modifier.height(12.dp))

        /* -------- CART TABLE -------- */

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .horizontalScroll(scroll)
                .border(1.dp,Color.LightGray)
        ){

            Row(
                modifier = Modifier.background(Color(0xFF1976D2))
            ){

                HeaderCell("Product",160.dp,Color.White)
                HeaderCell("Qty",80.dp,Color.White)
                HeaderCell("MRP",80.dp,Color.White)
                HeaderCell("Sell",80.dp,Color.White)
                HeaderCell("Disc%",80.dp,Color.White)
                HeaderCell("Total",100.dp,Color.White)
                HeaderCell("Action",70.dp,Color.White)
            }

            cart.forEachIndexed{index,item->

                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){

                    Column(modifier = Modifier.width(160.dp)){

                        Text(item.title)

                        Text(
                            "Stock ${item.stock} ${item.unit}",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    var qtyText by remember(item.qty) {
                        mutableStateOf(format2(item.qty))
                    }

                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { newValue ->

                            if(newValue.count { it == '.' } > 1) return@OutlinedTextField

                            val q = newValue.toDoubleOrNull()

                            if(q == null){

                                qtyText = ""

                            }
                            else if(q > item.stock){

                                Toast.makeText(
                                    context,
                                    "Stock only ${item.stock}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                qtyText = format2(item.stock)

                                cart[index] = item.copy(qty = item.stock)

                            }
                            else{

                                qtyText = newValue
                                cart[index] = item.copy(qty = q)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )

                    TableCell("₹${format2(item.mrp)}",80.dp)
                    TableCell("₹${format2(item.price)}",80.dp)
                    TableCell("${format2(item.discountPercent)}%",80.dp)

                    TableCell(
                        "₹${format2(item.price*item.qty)}",
                        100.dp
                    )

                    IconButton(
                        onClick = { cart.removeAt(index) }
                    ){
                        Icon(Icons.Default.Delete,null,tint=Color.Red)
                    }
                }

                Divider()
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){

            OutlinedTextField(
                value = discountValue,
                onValueChange = { discountValue = it },
                label = { Text("Discount Amount") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.weight(1f)
            )

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
        Spacer(Modifier.height(10.dp))

        /* -------- TOTAL -------- */

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F7FA)
            )
        ){

            Column(
                modifier = Modifier.padding(16.dp)
            ){

                SummaryRow("Subtotal",subtotal)

                SummaryRow("Discount",discountAmount)

                SummaryRow("GST",gstAmount)

                Divider()

                SummaryRow(
                    "Total",
                    total,
                    isTotal = true
                )
            }
        }

        /* -------- PAYMENT -------- */

        OutlinedTextField(
            value = paid,
            onValueChange = { v ->

                val p = v.toDoubleOrNull()

                if(p != null && p <= total){
                    paid = v
                }
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

                scope.launch{

                    if(buyerPhone.length != 10){

                        Toast.makeText(
                            context,
                            "Enter valid 10 digit phone",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    if(cart.isEmpty()){

                        Toast.makeText(
                            context,
                            "Add product first",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    loading = true

                    try{

                        val items = cart.map{

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
                                discountAmount,
                                gstAmount,
                                paidAmount,
                                json
                            )

                        val billNo = res.data.bill_no

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://jminnovatech.xyz/enjoybazar/invoice/$billNo")
                        )

                        context.startActivity(intent)

                    }catch(_:Exception){}

                    loading = false
                }

            },
            modifier = Modifier.fillMaxWidth()
        ){

            if(loading)
                CircularProgressIndicator()
            else
                Text("Save Bill")
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