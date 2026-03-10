package com.jminnovatech.joymart.ui.distributor.sales

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.data.model.distributor.BillItem

data class ProductItem(
    val id:Int,
    val title:String,
    val price:Double,
    val stock:Double
)

data class CartItem(
    val id:Int,
    val title:String,
    val price:Double,
    val qty:Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorBillCreateScreen(){

    val scope = rememberCoroutineScope()

    var bills by remember{ mutableStateOf(listOf<BillItem>()) }
    var search by remember{ mutableStateOf("") }
    var loading by remember{ mutableStateOf(true) }
    var showPOS by remember{ mutableStateOf(false) }

    LaunchedEffect(Unit){

        try{

            val r = RetrofitClient.distributorApi.getSales()

            bills = r.data?.data ?: emptyList()

        }catch(_:Exception){}

        loading=false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ){

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){

            Text(
                "Billing",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick={showPOS=true}
            ){
                Icon(Icons.Default.Add,null)
                Spacer(Modifier.width(6.dp))
                Text("Create Bill")
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value=search,
            onValueChange={search=it},
            label={Text("Search Bill")},
            modifier=Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        if(loading){

            Box(
                modifier=Modifier.fillMaxSize(),
                contentAlignment=Alignment.Center
            ){
                CircularProgressIndicator()
            }

        }else{

            LazyColumn{

                items(
                    bills.filter{
                        it.bill_no.contains(search,true) ||
                                (it.buyer_name ?: "").contains(search,true)
                    }
                ){

                    BillCard(it)
                }
            }
        }
    }

    if(showPOS){

        ModalBottomSheet(
            onDismissRequest={showPOS=false}
        ){

            CreateBillPOS()
        }
    }
}

@Composable
fun BillCard(bill:BillItem){

    val context = LocalContext.current

    Card(
        modifier=Modifier
            .fillMaxWidth()
            .padding(vertical=6.dp),
        shape=RoundedCornerShape(12.dp)
    ){

        Column(
            modifier=Modifier.padding(12.dp)
        ){

            Text("Bill : ${bill.bill_no}")

            Text("Customer : ${bill.buyer_name ?: "-"}")

            Text("Total : ₹${bill.total}")

            Spacer(Modifier.height(6.dp))

            Button(
                onClick={

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://jminnovatech.xyz/enjoybazar/invoice/${bill.bill_no}"
                        )
                    )

                    context.startActivity(intent)
                }
            ){

                Text("View Invoice")

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBillPOS(){

    val scope = rememberCoroutineScope()
    val snackbar = remember{ SnackbarHostState() }

    var products by remember{ mutableStateOf(listOf<ProductItem>()) }
    val cart = remember{ mutableStateListOf<CartItem>() }

    var buyerName by remember{ mutableStateOf("") }
    var buyerPhone by remember{ mutableStateOf("") }
    var buyerAddress by remember{ mutableStateOf("") }

    var productSearch by remember{ mutableStateOf("") }

    var discountType by remember{ mutableStateOf("Amount") }
    var discountValue by remember{ mutableStateOf("0") }

    var gst by remember{ mutableStateOf("0") }
    var paid by remember{ mutableStateOf("0") }

    var loading by remember{ mutableStateOf(false) }

    LaunchedEffect(Unit){

        try{

            val r = RetrofitClient.distributorApi.getProducts()

            products = r.data?.data?.map{

                ProductItem(
                    id = it.id,
                    title = it.title,
                    price = it.sell_price.toDouble(),
                    stock = it.stock_qty.toDouble()
                )

            } ?: emptyList()

        }catch(_:Exception){}
    }

    val subtotal = cart.sumOf{ it.price * it.qty }

    val discountNum = discountValue.toDoubleOrNull() ?: 0.0

    val discountAmount =
        if(discountType=="Amount")
            discountNum
        else
            subtotal * discountNum / 100

    val gstAmount =
        (subtotal-discountAmount) *
                (gst.toDoubleOrNull() ?: 0.0) / 100

    val total = (subtotal-discountAmount)+gstAmount

    val paidAmount = paid.toDoubleOrNull() ?: 0.0

    val due = total-paidAmount

    val filteredProducts =
        products.filter{
            it.title.contains(productSearch,true)
        }

    Scaffold(
        snackbarHost={ SnackbarHost(snackbar) }
    ){pad->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ){

            Text(
                "Create Bill",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value=buyerName,
                onValueChange={buyerName=it},
                label={Text("Customer Name")},
                modifier=Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value=buyerPhone,
                onValueChange={buyerPhone=it},
                label={Text("Phone")},
                modifier=Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value=buyerAddress,
                onValueChange={buyerAddress=it},
                label={Text("Address")},
                modifier=Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value=productSearch,
                onValueChange={productSearch=it},
                label={Text("Search Product")},
                modifier=Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(250.dp)
            ){

                items(filteredProducts){p->

                    Card(
                        modifier=Modifier.padding(6.dp),
                        shape=RoundedCornerShape(12.dp)
                    ){

                        Column(
                            Modifier.padding(10.dp)
                        ){

                            Text(p.title)

                            Text(
                                "₹${p.price}",
                                color=Color.Gray
                            )

                            Spacer(Modifier.height(6.dp))

                            Button(
                                onClick={

                                    val index =
                                        cart.indexOfFirst{it.id==p.id}

                                    if(index!=-1){

                                        val item=cart[index]

                                        cart[index] =
                                            item.copy(qty=item.qty+1)

                                    }else{

                                        cart.add(
                                            CartItem(
                                                id=p.id,
                                                title=p.title,
                                                price=p.price,
                                                qty=1.0
                                            )
                                        )
                                    }
                                },
                                modifier=Modifier.fillMaxWidth()
                            ){
                                Text("Add")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(cart.isNotEmpty()){

                Column{

                    Text("Cart")

                    LazyColumn(
                        modifier=Modifier.height(150.dp)
                    ){

                        items(cart){item->

                            Row(
                                modifier=Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment=Alignment.CenterVertically
                            ){

                                Column(
                                    modifier=Modifier.weight(1f)
                                ){

                                    Text(item.title)

                                    Text("₹${item.price * item.qty}")
                                }

                                IconButton(
                                    onClick={

                                        val index =
                                            cart.indexOf(item)

                                        if(item.qty>1){

                                            cart[index] =
                                                item.copy(qty=item.qty-1)

                                        }
                                    }
                                ){
                                    Icon(Icons.Default.Remove,null)
                                }

                                Text(item.qty.toString())

                                IconButton(
                                    onClick={

                                        val index =
                                            cart.indexOf(item)

                                        cart[index] =
                                            item.copy(qty=item.qty+1)
                                    }
                                ){
                                    Icon(Icons.Default.Add,null)
                                }

                                IconButton(
                                    onClick={cart.remove(item)}
                                ){
                                    Icon(Icons.Default.Delete,null)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("Subtotal : ₹$subtotal")
            Text("Total : ₹$total")
            Text(
                "Due : ₹$due",
                color=if(due>0)Color.Red else Color.Green
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick={

                    if(buyerName.isEmpty()){

                        scope.launch{
                            snackbar.showSnackbar(
                                "Customer name required"
                            )
                        }

                        return@Button
                    }

                    scope.launch{

                        loading=true

                        try{

                            val items =
                                cart.map{
                                    mapOf(
                                        "id" to it.id,
                                        "qty" to it.qty,
                                        "price" to it.price
                                    )
                                }

                            val json =
                                Gson().toJson(items)

                            RetrofitClient
                                .distributorApi
                                .createSale(
                                    buyerName,
                                    discountAmount,
                                    gstAmount,
                                    paidAmount,
                                    json
                                )

                            snackbar.showSnackbar("Bill Created")

                            cart.clear()

                        }catch(_:Exception){

                            snackbar.showSnackbar("Failed")
                        }

                        loading=false
                    }
                },
                modifier=Modifier.fillMaxWidth()
            ){

                if(loading)
                    CircularProgressIndicator(
                        color=Color.White,
                        modifier=Modifier.size(18.dp)
                    )
                else
                    Text("Generate Bill")
            }
        }
    }
}