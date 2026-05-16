package com.jminnovatech.joymart.ui.distributor.offline


import android.content.Context
import android.widget.Toast
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.ui.distributor.sales.ProductItem
import kotlinx.coroutines.flow.first
import com.jminnovatech.joymart.ui.distributor.sales.CartItem
private val Context.dataStore by preferencesDataStore("offline_bill_store")

object OfflineBillManager {

    private val KEY =
        stringPreferencesKey("offline_bills")
    private val PRODUCT_KEY =
        stringPreferencesKey("offline_products")
    data class OfflineBill(

        val localId:String,

        val buyerName:String,
        val buyerPhone:String,
        val buyerAddress:String,

        val discount:Double,
        val tax:Double,
        val paid:Double,

        val items:String,

        val total:Double,

        val synced:Boolean = false,

        val billNo:String? = null,

        val createdAt:Long =
            System.currentTimeMillis()
    )
    suspend fun saveProducts(
        context: Context,
        products: List<ProductItem>
    ) {

        context.dataStore.edit {

            it[PRODUCT_KEY] =
                Gson().toJson(products)
        }
    }
    suspend fun getProducts(
        context: Context
    ): List<ProductItem> {

        val json =
            context.dataStore.data.first()[PRODUCT_KEY]
                ?: return emptyList()

        return try {

            Gson().fromJson(
                json,
                object :
                    TypeToken<List<ProductItem>>() {}.type
            )

        } catch (_: Exception) {

            emptyList()
        }
    }
    suspend fun saveOffline(
        context: Context,
        bill: OfflineBill
    ) {

        val list = getBills(context).toMutableList()

        list.add(0, bill)

        context.dataStore.edit {

            it[KEY] = Gson().toJson(list)
        }
    }

    suspend fun getBills(
        context: Context
    ): List<OfflineBill> {

        val json =
            context.dataStore.data.first()[KEY]
                ?: return emptyList()

        return try {

            Gson().fromJson(
                json,
                object :
                    TypeToken<List<OfflineBill>>() {}.type
            )

        } catch (_: Exception) {

            emptyList()
        }
    }

    suspend fun syncBills(
        context: Context
    ) {

        val bills =
            getBills(context).toMutableList()

        var changed = false

        bills.forEachIndexed { index, bill ->

            if (!bill.synced) {

                try {

                    val res =
                        RetrofitClient
                            .distributorApi
                            .createSale(
                                buyerName = bill.buyerName,
                                buyerPhone = bill.buyerPhone,
                                buyerAddress = bill.buyerAddress,
                                discount = bill.discount,
                                tax = bill.tax,
                                paid = bill.paid,
                                items = bill.items
                            )

                    bills[index] =
                        bill.copy(
                            synced = true,
                            billNo = res.data.bill_no
                        )

                    changed = true

                } catch (_: Exception) {

                }
            }
        }

        if (changed) {

            context.dataStore.edit {

                it[KEY] = Gson().toJson(bills)
            }
        }
    }

    suspend fun clearSynced(
        context: Context
    ) {

        val pending =
            getBills(context)
                .filter { !it.synced }

        context.dataStore.edit {

            it[KEY] = Gson().toJson(pending)
        }

    }
    suspend fun reduceOfflineStock(
        context: Context,
        cart: List<CartItem>
    ) {

        val products =
            getProducts(context).toMutableList()

        cart.forEach { cartItem ->

            val index =
                products.indexOfFirst {
                    it.id == cartItem.id
                }

            if (index != -1) {

                val p = products[index]

                products[index] =
                    p.copy(
                        stock =
                            (p.stock - cartItem.qty)
                                .coerceAtLeast(0.0)
                    )
            }
        }

        saveProducts(context, products)
    }
    suspend fun deleteBill(
        context: Context,
        bill: OfflineBill
    ) {

        val bills =
            getBills(context)
                .filter { it.localId != bill.localId }

        context.dataStore.edit {

            it[KEY] = Gson().toJson(bills)
        }
    }
    suspend fun restoreStock(
        context: Context,
        bill: OfflineBill
    ) {

        try {

            val cartType =
                object :
                    TypeToken<List<Map<String, Double>>>() {}.type

            val items:
                    List<Map<String, Double>> =
                Gson().fromJson(
                    bill.items,
                    cartType
                )

            val products =
                getProducts(context)
                    .toMutableList()

            items.forEach { item ->

                val id =
                    item["id"]?.toInt() ?: 0

                val qty =
                    item["qty"] ?: 0.0

                val index =
                    products.indexOfFirst {
                        it.id == id
                    }

                if (index != -1) {

                    val p = products[index]

                    products[index] =
                        p.copy(
                            stock = p.stock + qty
                        )
                }
            }

            saveProducts(
                context,
                products
            )

        } catch (_: Exception) {

        }
    }
    suspend fun syncSingleBill(
        context: Context,
        bill: OfflineBill
    ): Boolean {

        return try {

            val res =
                RetrofitClient
                    .distributorApi
                    .createSale(
                        buyerName = bill.buyerName,
                        buyerPhone = bill.buyerPhone,
                        buyerAddress = bill.buyerAddress,
                        discount = bill.discount,
                        tax = bill.tax,
                        paid = bill.paid,
                        items = bill.items
                    )

            val bills =
                getBills(context).toMutableList()

            val index =
                bills.indexOfFirst {
                    it.localId == bill.localId
                }

            if (index != -1) {

                bills[index] =
                    bill.copy(
                        synced = true,
                        billNo = res.data.bill_no
                    )

                context.dataStore.edit {

                    it[KEY] = Gson().toJson(bills)
                }
            }

            true

        } catch (e: Exception) {

            e.printStackTrace()

            false
        }
    }
}