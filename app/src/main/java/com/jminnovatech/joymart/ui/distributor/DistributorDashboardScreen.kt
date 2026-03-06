package com.jminnovatech.joymart.ui.distributor

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.data.model.distributor.DashboardSummary
import com.jminnovatech.joymart.data.model.distributor.LowStockProduct
import com.jminnovatech.joymart.data.model.distributor.RecentOrder

@Composable
fun DistributorDashboardScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var dashboard by remember { mutableStateOf<DashboardSummary?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var chartData by remember { mutableStateOf<List<Float>>(emptyList()) }
    var lowStock by remember { mutableStateOf<List<LowStockProduct>>(emptyList()) }
    var orders by remember { mutableStateOf<List<RecentOrder>>(emptyList()) }

    suspend fun loadDashboard(){

        try{

            val summary = api.getDashboardSummary()
            dashboard = summary

            val chart = api.getSalesChart()
            chartData = chart.data?.map { it.sales } ?: emptyList()

            val stock = api.getLowStock()
            lowStock = stock.data ?: emptyList()

            val ord = api.getRecentOrders()
            orders = ord.data ?: emptyList()

        }catch(e:Exception){
            e.printStackTrace()
        }

        loading = false
        refreshing = false
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(refreshing),
        onRefresh = {
            refreshing = true
            scope.launch { loadDashboard() }
        }
    ) {

        if (loading) {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FB))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

//                item {
//
//                    Text(
//                        "Distributor Dashboard",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                }

                item {

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        item {
                            DashboardCard(
                                "My Products",
                                "${dashboard?.products ?: 0}",
                                Color(0xFF2962FF),
                                Icons.Default.Inventory
                            )
                        }

                        item {
                            DashboardCard(
                                "Total Sales",
                                "${dashboard?.total_sales ?: 0}",
                                Color(0xFF2E7D32),
                                Icons.Default.ShoppingCart
                            )
                        }

                        item {
                            DashboardCard(
                                "Today Profit",
                                "₹${dashboard?.today_profit ?: 0}",
                                Color(0xFFF9A825),
                                Icons.Default.AttachMoney
                            )
                        }

                        item {
                            DashboardCard(
                                "Monthly Profit",
                                "₹${dashboard?.monthly_profit ?: 0}",
                                Color(0xFF424242),
                                Icons.AutoMirrored.Filled.TrendingUp
                            )
                        }

                        item {
                            DashboardCard(
                                "Total Profit",
                                "₹${dashboard?.total_profit ?: 0}",
                                Color(0xFF455A64),
                                Icons.Default.AccountBalance
                            )
                        }

                        item {
                            DashboardCard(
                                "Wallet",
                                "₹${dashboard?.wallet ?: 0}",
                                Color(0xFF00ACC1),
                                Icons.Default.AccountBalanceWallet
                            )
                        }
                    }
                }

                item {

                    Text(
                        "Sales Chart (7 days)",
                        fontWeight = FontWeight.Bold
                    )

                    SalesChart(chartData)
                }

                item {
                    LowStockSection(lowStock)
                }

                item {
                    RecentOrders(orders)
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector
) {

    val numericValue = value.replace("[^0-9.]".toRegex(),"").toFloatOrNull() ?: 0f

    val animatedValue by animateFloatAsState(
        targetValue = numericValue,
        animationSpec = tween(800),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White,
                            color.copy(alpha = 0.10f)
                        )
                    )
                )
                .border(
                    1.2.dp,
                    color.copy(alpha = 0.35f),
                    RoundedCornerShape(16.dp)
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        if(value.contains("₹"))
                            "₹${animatedValue.toInt()}"
                        else
                            animatedValue.toInt().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )

                    Spacer(Modifier.height(5.dp))

                    MiniSparkline(color)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun MiniSparkline(color: Color) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {

        val points = listOf(2f,5f,3f,6f,4f,7f)

        val step = size.width / points.size

        for (i in 0 until points.size - 1) {

            drawLine(
                color = color,
                start = Offset(step*i,size.height-points[i]),
                end = Offset(step*(i+1),size.height-points[i+1]),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }
}
@Composable
fun SalesChart(data: List<Float>) {

    AndroidView(
        factory = { context ->

            val chart = LineChart(context)

            val entries = data.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }

            val dataSet = LineDataSet(entries, "Sales")

            dataSet.apply {

                lineWidth = 3f
                color = AndroidColor.BLUE
                setDrawValues(false)
                setDrawCircles(true)
                circleRadius = 5f
            }

            chart.data = LineData(dataSet)

            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false

            chart.setOnChartValueSelectedListener(object :
                OnChartValueSelectedListener {

                override fun onValueSelected(e: Entry?, h: Highlight?) {

                    e?.let {

                        Toast.makeText(
                            context,
                            "Sales ₹${it.y}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onNothingSelected() {}
            })

            chart
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

@Composable
fun LowStockSection(list:List<LowStockProduct>){

    Column{

        Text("⚠ Low Stock Products", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        list.forEach{

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ){

                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Text(it.title)

                    Text(
                        "Stock ${it.stock_qty}",
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun RecentOrders(list:List<RecentOrder>){

    Column{

        Text("Recent Orders", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        list.forEach{

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ){

                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Text("Bill #${it.bill_no}")

                    Text(
                        "₹${it.total}",
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}