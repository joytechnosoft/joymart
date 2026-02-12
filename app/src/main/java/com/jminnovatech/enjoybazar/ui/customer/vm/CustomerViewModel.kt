package com.jminnovatech.joymart.ui.customer.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jminnovatech.data.model.customer.CustomerCartItem
import com.jminnovatech.data.model.customer.CustomerOrder
import com.jminnovatech.joymart.data.model.customer.CustomerOrderCreateRequest
import com.jminnovatech.joymart.data.model.customer.CustomerOrderItemRequest
import com.jminnovatech.joymart.data.model.customer.CustomerProduct
import com.jminnovatech.joymart.data.model.customer.CustomerProfile
import com.jminnovatech.joymart.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CustomerViewModel : ViewModel() {

    private val repo = CustomerRepository()

    // ---------------- PRODUCTS ----------------
    private val _products = MutableStateFlow<List<CustomerProduct>>(emptyList())
    val products = _products.asStateFlow()

    // ---------------- ORDERS ----------------
    private val _orders = MutableStateFlow<List<CustomerOrder>>(emptyList())
    val orders = _orders.asStateFlow()

    // ---------------- CART ----------------
    private val _cart = MutableStateFlow<List<CustomerCartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    // ---------------- PROFILE ----------------
    private val _profile = MutableStateFlow<CustomerProfile?>(null)
    val profile = _profile.asStateFlow()

    // ---------------- PRODUCTS ----------------

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val res = repo.getProducts()
                if (res.success) {
                    _products.value = res.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("PRODUCTS", "Error loading products", e)
            }
        }
    }

    // ---------------- CART ----------------

    fun addToCart(product: CustomerProduct) {
        val list = _cart.value.toMutableList()
        val index = list.indexOfFirst { it.product.id == product.id }

        if (index >= 0) {
            list[index] = list[index].copy(qty = list[index].qty + 1)
        } else {
            list.add(CustomerCartItem(product, 1.0))
        }
        _cart.value = list
    }

    fun removeFromCart(product: CustomerProduct) {
        val list = _cart.value.toMutableList()
        val index = list.indexOfFirst { it.product.id == product.id }

        if (index >= 0) {
            val item = list[index]
            if (item.qty > 1) {
                list[index] = item.copy(qty = item.qty - 1)
            } else {
                list.removeAt(index)
            }
        }
        _cart.value = list
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // ---------------- ORDERS ----------------

    fun loadOrders() {
        viewModelScope.launch {
            try {
                val res = repo.getOrders()
                Log.d("ORDERS_API", "Response = $res")

                if (res.success) {
                    _orders.value = res.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ORDERS_API", "Error", e)
            }
        }
    }

    // ---------------- PLACE ORDER ----------------
    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess = _orderSuccess.asStateFlow()

    fun clearOrderSuccess() {
        _orderSuccess.value = false
    }
    fun placeOrder(
        buyerName: String,
        buyerPhone: String,
        buyerAddress: String
    ) {
        viewModelScope.launch {

            if (_cart.value.isEmpty()) {
                Log.e("ORDER", "Cart is empty")
                return@launch
            }

            val items = _cart.value.map {
                CustomerOrderItemRequest(
                    product_id = it.product.id,
                    qty = it.qty
                )
            }

            val request = CustomerOrderCreateRequest(
                buyer_name = buyerName,
                buyer_phone = buyerPhone,
                buyer_address = buyerAddress,
                items = items
            )

            try {
                val res = repo.placeOrder(request)
                if (res.success) {
                    _cart.value = emptyList()
                    _orderSuccess.value = true   // ✅ THIS LINE
                }
            }
             catch (e: HttpException) {
                Log.e("ORDER", "HTTP ${e.code()} ${e.response()?.errorBody()?.string()}")
            }
        }
    }

    // ---------------- PROFILE ----------------

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val res = repo.getCustomerProfile()
                if (res.success) {
                    _profile.value = res.data
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "Error loading profile", e)
            }
        }
    }

    fun saveProfile(
        name: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            try {
                val res = repo.updateCustomerProfile(name, phone, address)
                if (res.success) {
                    loadProfile() // refresh after save
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "Error saving profile", e)
            }
        }
    }



}
