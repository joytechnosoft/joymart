package com.jminnovatech.joymart.data.model.distributor

data class DashboardSummary(

    val products: Int,
    val total_sales: Int,
    val today_profit: Double,
    val monthly_profit: Double,
    val total_profit: Double,
    val wallet: Double

)

data class SalesChartItem(
    val date:String,
    val sales:Float
)

data class LowStockProduct(
    val id:Int,
    val title:String,
    val stock_qty:Int
)

data class RecentOrder(

    val id:Int,
    val bill_no:String,
    val total:Double,
    val payment_status:String

)

data class ProfitItem(
    val date:String,
    val total:Double
)