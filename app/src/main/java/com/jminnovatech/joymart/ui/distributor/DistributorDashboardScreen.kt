package com.jminnovatech.joymart.ui.distributor

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.data.model.distributor.DashboardSummary
import com.jminnovatech.joymart.data.model.distributor.LowStockProduct
import com.jminnovatech.joymart.data.model.distributor.RecentOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.formatter.ValueFormatter
@Composable
fun DistributorDashboardScreen(navController: NavHostController) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()

    var dashboard by remember { mutableStateOf<DashboardSummary?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var chartData by remember { mutableStateOf<List<Float>>(emptyList()) }
    var lowStock by remember { mutableStateOf<List<LowStockProduct>>(emptyList()) }
    var orders by remember { mutableStateOf<List<RecentOrder>>(emptyList()) }
    var showProductsModal by remember { mutableStateOf(false) }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadDashboard() {

        try {

            val summary = withContext(Dispatchers.IO) {
                api.getDashboardSummary()
            }

            val chart = withContext(Dispatchers.IO) {
                api.getSalesChart()
            }

            val stock = withContext(Dispatchers.IO) {
                api.getLowStock()
            }

            val ord = withContext(Dispatchers.IO) {
                api.getRecentOrders()
            }

            dashboard = summary
            val raw = chart.data?.map { it.sales } ?: emptyList()

            val weekData = MutableList(7) { 0f }

            chart.data?.forEach {

                val date = java.time.LocalDate.parse(it.date)
                val dayIndex = date.dayOfWeek.value - 1   // Mon=0 ... Sun=6

                weekData[dayIndex] = it.sales
            }

            chartData = weekData

            lowStock = stock.data ?: emptyList()
            orders = ord.data ?: emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        loading = false
        refreshing = false
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    LaunchedEffect(orders){

        if(orders.isNotEmpty()){

            println("New order: ${orders.first().bill_no}")

        }

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

                // -------- Horizontal Menu (new) --------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        MenuCard(
                            title = "Products",
                            icon = Icons.Default.Inventory,
                            color = Color(0xFF2962FF),
                            onClick = {
                                navController.navigate("products")
                            }
                        )

                        MenuCard(
                            title = "Create Bill",
                            icon = Icons.Default.PointOfSale,
                            color = Color(0xFF2E7D32),
                            onClick = {navController.navigate("create_bill") }
                        )

                        MenuCard(
                            title = "Payments",
                            icon = Icons.Default.Payments,
                            color = Color(0xFFEF6C00),
                            onClick = { }
                        )

                        MenuCard(
                            title = "Orders",
                            icon = Icons.Default.ShoppingBag,
                            color = Color(0xFF8E24AA),
                            onClick = { }
                        )

                        MenuCard(
                            title = "Verify",
                            icon = Icons.Default.VerifiedUser,
                            color = Color(0xFFD32F2F),
                            onClick = { }
                        )

                        MenuCard(
                            title = "Reports",
                            icon = Icons.Default.BarChart,
                            color = Color(0xFF455A64),
                            onClick = { }
                        )

                    }
                }

                // -------- Your Existing Dashboard Grid (unchanged) --------
                item {

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(210.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
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

                    val totalSales = chartData.sum()
                    val totalProfit = totalSales * 0.2f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        "Sales Analytics",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        "Last 7 days performance",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {

                                    Text(
                                        "₹${totalSales.toInt()}",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                    val lastWeek = totalSales * 0.85f
                                    val growth = ((totalSales - lastWeek) / lastWeek * 100)

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            if (growth >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (growth >= 0) Color(0xFF2E7D32) else Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(Modifier.width(4.dp))

                                        Text(
                                            "${growth.toInt()}% this week",
                                            fontSize = 12.sp,
                                            color = if (growth >= 0) Color(0xFF2E7D32) else Color.Red
                                        )
                                    }
                                    Text(
                                        "Profit ₹${totalProfit.toInt()}",
                                        color = Color(0xFFFF9800),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            WeeklySummary(chartData)

                            Spacer(Modifier.height(8.dp))

                            SalesChart(chartData)
                        }
                    }
                }

//                item {
//                    DailySummary(chartData)
//                }


                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                "Sales vs Profit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                "Daily comparison",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(Modifier.height(8.dp))

                            ProfitComparisonChart(chartData)
                        }
                    }
                }

                item {
                    InventoryAlertWidget(lowStock)
                }

                item {
                    TopSellingProducts()
                }

                item {
                    LowStockSection(lowStock)
                }

                item {
                    RecentOrders(orders)
                }
            }
            if (showProductsModal) {

                Dialog(
                    onDismissRequest = { showProductsModal = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF5F7FB)
                    ) {

                        Column {

                            // 🔹 Top bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    "Products",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { showProductsModal = false }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            Divider()

                            // 🔹 Your existing screen
                            DistributorProductScreen(refreshTrigger = 0)

                        }
                    }
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
    Column {

        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(3.dp))

        Row {

            Text(
                if(value.contains("₹"))
                    "₹${animatedValue.toInt()}"
                else
                    animatedValue.toInt().toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(Modifier.width(4.dp))

            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.height(5.dp))

        MiniSparkline(color)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
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

            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = true
            chart.setDrawGridBackground(false)

            chart.setExtraOffsets(12f,10f,12f,20f)

            chart.axisLeft.apply {

                setDrawGridLines(true)
                axisMinimum = 0f
                granularity = 1f
                setDrawZeroLine(false)

                textColor = AndroidColor.DKGRAY
            }

            chart
        },

        update = { chart ->

            val allDays = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

            val todayIndex = (java.util.Calendar.getInstance()
                .get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

            val days = (todayIndex + 1..todayIndex + 7)
                .map { allDays[it % 7] }

            chart.xAxis.apply {

                valueFormatter = IndexAxisValueFormatter(days)

                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)

                textColor = AndroidColor.DKGRAY
            }

            val rotatedData = (todayIndex + 1..todayIndex + 7)
                .map { data[it % 7] }

            val salesEntries = rotatedData.mapIndexed { i,v ->
                Entry(i.toFloat(),v)
            }

            val profitEntries = rotatedData.mapIndexed { i,v ->
                Entry(i.toFloat(),v * 0.2f)
            }

            val salesSet = LineDataSet(salesEntries,"Sales").apply {

                lineWidth = 3f

                setDrawValues(true)
                setDrawCircles(true)

                circleRadius = 5f
                circleHoleRadius = 2f

                setCircleColor(AndroidColor.parseColor("#2962FF"))
                setCircleHoleColor(AndroidColor.WHITE)

                color = AndroidColor.parseColor("#2962FF")

                mode = LineDataSet.Mode.CUBIC_BEZIER

                setDrawHighlightIndicators(true)
                highLightColor = AndroidColor.parseColor("#FF5722")
                highlightLineWidth = 1.5f

                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        AndroidColor.parseColor("#4A90E2"),
                        AndroidColor.parseColor("#00FFFFFF")
                    )
                )

                fillDrawable = gradient
                setDrawFilled(true)

                valueFormatter = object : ValueFormatter() {

                    override fun getPointLabel(entry: Entry?): String {

                        return if (entry?.y == 0f) "" else entry!!.y.toInt().toString()

                    }
                }
            }

            val profitSet = LineDataSet(profitEntries,"Profit").apply {

                lineWidth = 3f

                setDrawValues(true)
                setDrawCircles(true)

                circleRadius = 4f

                setCircleColor(AndroidColor.parseColor("#FF9800"))
                setCircleHoleColor(AndroidColor.WHITE)

                color = AndroidColor.parseColor("#FF9800")

                mode = LineDataSet.Mode.CUBIC_BEZIER

                valueFormatter = object : ValueFormatter() {

                    override fun getPointLabel(entry: Entry?): String {

                        return if (entry?.y == 0f) "" else entry!!.y.toInt().toString()

                    }
                }
            }

            chart.data = LineData(salesSet,profitSet)

            chart.marker = object : MarkerView(chart.context, android.R.layout.simple_list_item_1) {

                override fun refreshContent(e: Entry?, highlight: Highlight?) {

                    e?.let {

                        val tv = findViewById<TextView>(android.R.id.text1)

                        val bg = GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT,
                            intArrayOf(
                                AndroidColor.parseColor("#E3F2FD"),
                                AndroidColor.parseColor("#FFFFFF")
                            )
                        )

                        tv.background = bg

                        tv.setTextColor(AndroidColor.parseColor("#333333"))

                        tv.setPadding(20,12,20,12)

                        val sales : Float
                        val profit : Float

                        if(highlight?.dataSetIndex == 0){

                            sales = it.y
                            profit = sales * 0.2f

                        }else{

                            profit = it.y
                            sales = profit / 0.2f
                        }

                        tv.text =
                            "Sales ₹${sales.toInt()}\nProfit ₹${profit.toInt()}"
                    }

                    super.refreshContent(e, highlight)
                }

                override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {

                    val offsetX =
                        if (posX > chart.width * 0.8f)
                            -width.toFloat()
                        else
                            -(width / 2).toFloat()

                    return MPPointF(offsetX, -height.toFloat())
                }
            }

            chart.animateX(800)

            // 🔥 highlight today (always last index)
            chart.highlightValue(6f,0)

            chart.invalidate()
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    )
}
@Composable
fun InventoryAlertWidget(list:List<LowStockProduct>){

    if(list.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ){

        Column(
            Modifier.padding(16.dp)
        ){

            Text(
                "Inventory Alert",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6F00)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "${list.size} products are running low stock"
            )
        }
    }
}
@Composable
fun ProfitComparisonChart(data: List<Float>) {

    AndroidView(

        factory = { context ->

            val chart = BarChart(context)

            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = true
            chart.legend.textColor = AndroidColor.DKGRAY

            chart.setFitBars(true)
            chart.setDrawValueAboveBar(true)

            chart.setExtraOffsets(12f,10f,12f,20f)

            chart.axisLeft.apply {

                axisMinimum = 0f
                granularity = 1f
                setDrawGridLines(true)
                setDrawZeroLine(false)

                textColor = AndroidColor.DKGRAY
            }

            chart
        },

        update = { chart ->

            val allDays = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

            // today index API 24 compatible
            val todayIndex = (java.util.Calendar.getInstance()
                .get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

            // rotate days
            val days = (todayIndex + 1..todayIndex + 7)
                .map { allDays[it % 7] }

            chart.xAxis.apply {

                valueFormatter = IndexAxisValueFormatter(days)

                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)

                textColor = AndroidColor.DKGRAY
            }

            // rotate data
            val rotatedData = (todayIndex + 1..todayIndex + 7)
                .map { data[it % 7] }

            val salesEntries = rotatedData.mapIndexed { i,v ->
                BarEntry(i.toFloat(),v)
            }

            val profitEntries = rotatedData.mapIndexed { i,v ->
                BarEntry(i.toFloat(),v * 0.2f)
            }

            val salesSet = BarDataSet(salesEntries,"Sales").apply {

                color = AndroidColor.parseColor("#4CAF50")
                valueTextSize = 10f

                valueFormatter = object : ValueFormatter() {

                    override fun getBarLabel(barEntry: BarEntry?): String {

                        return if (barEntry?.y == 0f) "" else barEntry!!.y.toInt().toString()

                    }
                }
            }

            val profitSet = BarDataSet(profitEntries,"Profit").apply {

                color = AndroidColor.parseColor("#FF9800")
                valueTextSize = 10f

                valueFormatter = object : ValueFormatter() {

                    override fun getBarLabel(barEntry: BarEntry?): String {

                        return if (barEntry?.y == 0f) "" else barEntry!!.y.toInt().toString()

                    }
                }
            }

            val barData = BarData(salesSet,profitSet)

            barData.barWidth = 0.35f

            chart.data = barData

            chart.groupBars(
                0f,
                0.2f,
                0.05f
            )

            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = 7f

            chart.marker = object : MarkerView(chart.context, android.R.layout.simple_list_item_1) {

                override fun refreshContent(e: Entry?, highlight: Highlight?) {

                    e?.let {

                        val tv = findViewById<TextView>(android.R.id.text1)

                        val sales : Float
                        val profit : Float

                        if(highlight?.dataSetIndex == 0){

                            sales = it.y
                            profit = sales * 0.2f

                        }else{

                            profit = it.y
                            sales = profit / 0.2f
                        }

                        tv.setBackgroundColor(AndroidColor.WHITE)
                        tv.setTextColor(AndroidColor.parseColor("#333333"))

                        tv.setPadding(20,12,20,12)

                        tv.text =
                            "Sales ₹${sales.toInt()}\nProfit ₹${profit.toInt()}"
                    }

                    super.refreshContent(e, highlight)
                }

                override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {

                    val offsetX =
                        if (posX > chart.width * 0.8f)
                            -width.toFloat()
                        else
                            -(width / 2).toFloat()

                    return MPPointF(offsetX, -height.toFloat())
                }
            }

            chart.animateY(800)

            chart.invalidate()
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
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

@Composable
fun TopSellingProducts(){

    Card{

        Column(Modifier.padding(12.dp)){

            Text(
                "🔥 Top Selling Products",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            repeat(3){

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Text("Product ${it+1}")

                    Text(
                        "${10+it} sold",
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}



@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(3.dp))

        Text(
            title,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun DailySummary(data: List<Float>) {

    val sales = data.lastOrNull() ?: 0f
    val profit = sales * 0.2f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text("Today's Sales", fontWeight = FontWeight.Bold)
                Text("₹${sales.toInt()}", color = Color(0xFF2E7D32))
            }

            Column {
                Text("Today's Profit", fontWeight = FontWeight.Bold)
                Text("₹${profit.toInt()}", color = Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun DailyBreakdown(data: List<Float>) {

    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        days.forEachIndexed { index, day ->

            val sales = data.getOrNull(index) ?: 0f
            val profit = sales * 0.2f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "₹${sales.toInt()}",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32)
                )

                Text(
                    "₹${profit.toInt()}",
                    fontSize = 10.sp,
                    color = Color(0xFFFF9800)
                )

                Text(
                    day,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun WeeklySummary(data: List<Float>) {

    var expanded by remember { mutableStateOf(false) }

    val sales = data.sum()
    val profit = sales * 0.2f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { expanded = !expanded }
    ) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        "This Week Sales",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "₹${sales.toInt()}",
                        color = Color(0xFF2E7D32)
                    )
                }

                Column {

                    Text(
                        "This Week Profit",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "₹${profit.toInt()}",
                        color = Color(0xFFFF9800)
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {

                Divider()

                Spacer(Modifier.height(6.dp))

                DailyBreakdown(data)

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}