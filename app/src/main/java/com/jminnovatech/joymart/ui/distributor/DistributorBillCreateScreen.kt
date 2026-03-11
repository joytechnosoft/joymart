package com.jminnovatech.joymart.ui.distributor.sales

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import java.text.DecimalFormat

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorBillCreateScreen(){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf(listOf<ProductItem>()) }
    val cart = remember { mutableStateListOf<CartItem>() }

    var productMenu by remember { mutableStateOf(false) }

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerAddress by remember { mutableStateOf("") }

    var discountValue by remember { mutableStateOf("0") }
    var gst by remember { mutableStateOf("0") }
    var paid by remember { mutableStateOf("0") }

    var loading by remember { mutableStateOf(false)}

    val df = DecimalFormat("0.00")

    fun format2(v:Double):String{
        return df.format(v)
    }

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
        (subtotal - discountAmount) *
                (gst.toDoubleOrNull() ?: 0.0) / 100

    val total = subtotal - discountAmount + gstAmount

    val paidAmount = paid.toDoubleOrNull() ?: 0.0

    val due = total - paidAmount

    val paymentStatus =
        when{
            paidAmount == 0.0 -> "Due"
            paidAmount < total -> "Partial"
            else -> "Paid"
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){

        Text(
            "Create Bill",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        /* -------- CUSTOMER -------- */

        Card{

            Column(
                modifier = Modifier.padding(12.dp)
            ){

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = buyerPhone,
                    onValueChange = { buyerPhone = it },
                    label = { Text("Customer Phone") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = buyerAddress,
                    onValueChange = { buyerAddress = it },
                    label = { Text("Customer Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        /* -------- PRODUCT SELECT -------- */

        ExposedDropdownMenuBox(
            expanded = productMenu,
            onExpandedChange = { productMenu = !productMenu }
        ){

            OutlinedTextField(
                value = "Select Product",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text("Select Product") }
            )

            ExposedDropdownMenu(
                expanded = productMenu,
                onDismissRequest = { productMenu=false }
            ){

                products.forEach{p->

                    DropdownMenuItem(
                        text={ Text("${p.title} (₹${p.price}/${p.unit})") },
                        onClick={

                            productMenu=false

                            if(p.stock<=0){

                                Toast.makeText(
                                    context,
                                    "Out of stock",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }else{

                                if(cart.find{it.id==p.id}==null){

                                    cart.add(
                                        CartItem(
                                            id=p.id,
                                            title=p.title,
                                            price=p.price,
                                            mrp=p.mrp,
                                            unit=p.unit,
                                            discountPercent=p.discountPercent,
                                            stock=p.stock,
                                            qty=1.0
                                        )
                                    )

                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /* -------- CART TABLE -------- */

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
        ) {

            /* ---------- HEADER ---------- */

            Row(
                modifier = Modifier
                    .background(Color(0xFF1976D2))
                    .padding(vertical = 10.dp)
            ) {

                HeaderCell("Product",170.dp,Color.White)
                HeaderCell("Qty",80.dp,Color.White)
                HeaderCell("MRP",70.dp,Color.White)
                HeaderCell("Sell",70.dp,Color.White)
                HeaderCell("Disc%",70.dp,Color.White)
                HeaderCell("Total",90.dp,Color.White)
                HeaderCell("Action",60.dp,Color.White)
            }

            cart.forEachIndexed { index, item ->

                Row(
                    modifier = Modifier
                        .background(
                            if(index % 2 == 0)
                                Color.White
                            else
                                Color(0xFFF7F7F7)
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){

                    /* PRODUCT */

                    Column(
                        modifier = Modifier.width(170.dp)
                    ){

                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            "Stock ${format2(item.stock)} ${item.unit}",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    /* QTY INPUT */

                    var qtyText by remember { mutableStateOf(format2(item.qty)) }

                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { newValue ->

                            qtyText = newValue

                            val q = newValue.toDoubleOrNull()

                            if(q != null && q <= item.stock){

                                cart[index] = item.copy(qty = q)

                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .width(80.dp)
                            .height(50.dp)
                    )

                    /* MRP */

                    TableCell("₹${format2(item.mrp)}",70.dp)

                    /* SELL */

                    TableCell("₹${format2(item.price)}",70.dp)

                    /* DISC */

                    TableCell("${format2(item.discountPercent)}%",70.dp)

                    /* TOTAL */

                    TableCell(
                        "₹${format2(item.price * item.qty)}",
                        90.dp
                    )

                    /* DELETE */

                    Box(
                        modifier = Modifier.width(60.dp),
                        contentAlignment = Alignment.Center
                    ){

                        IconButton(
                            onClick = { cart.removeAt(index) }
                        ){
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                        }

                    }
                }

                Divider()
            }
        }

        Spacer(Modifier.height(16.dp))

        /* -------- DISCOUNT -------- */

        Row{

            OutlinedTextField(
                value = discountValue,
                onValueChange = { discountValue = it },
                label = { Text("Discount") },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = gst,
                onValueChange = { gst = it },
                label = { Text("GST %") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        /* -------- SUMMARY -------- */

        Text("Subtotal : ₹${format2(subtotal)}")

        Text("Discount : ₹${format2(discountAmount)}")

        Text("GST : ₹${format2(gstAmount)}")

        Text(
            "Total : ₹${format2(total)}",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        /* -------- PAYMENT -------- */

        Row{

            var paidText by remember { mutableStateOf("") }

            OutlinedTextField(
                value = paidText,
                onValueChange = { newValue ->

                    paidText = newValue

                    val v = newValue.toDoubleOrNull()

                    if (v != null) {

                        if (v <= total) {

                            paid = newValue

                        } else {

                            /* limit to total */

                            paidText = format2(total)
                            paid = format2(total)

                        }
                    }
                },
                label = { Text("Paid Amount") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = format2(due),
                onValueChange = {},
                readOnly = true,
                label = { Text("Due") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Payment Status : $paymentStatus")

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {

                scope.launch{

                    if(cart.isEmpty()){

                        Toast.makeText(
                            context,
                            "Add product first",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    loading=true

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

                    loading=false
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

/* -------- HELPER UI -------- */

@Composable
fun HeaderCell(
    text:String,
    width:Dp,
    color:Color
){

    Text(
        text = text,
        modifier = Modifier
            .width(width),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TableCell(
    text:String,
    width:Dp
){

    Text(
        text = text,
        modifier = Modifier
            .width(width),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}