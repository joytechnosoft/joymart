package com.jminnovatech.joymart.ui.distributor.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jminnovatech.joymart.data.model.customer.CustomerProfile
import com.jminnovatech.joymart.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DistributorViewModel : ViewModel() {

    private val repo = CustomerRepository()

    private val _profile = MutableStateFlow<CustomerProfile?>(null)
    val profile = _profile.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            val res = repo.getCustomerProfile()
            if (res.success) {
                _profile.value = res.data
            }
        }
    }

    fun saveProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repo.updateCustomerProfile(name, phone, address)
            loadProfile()
        }
    }
}
