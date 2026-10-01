package com.dadarberedar.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.data.remote.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loggedIn = authManager.isLoggedIn()
                _isLoggedIn.value = loggedIn
                if (!loggedIn) {
                    _error.value = "Silakan masuk terlebih dahulu"
                    _isLoading.value = false
                    return@launch
                }
                val token = authManager.getToken() ?: ""
                val res = apiService.getMyOrders("Bearer $token")
                if (res.isSuccessful) {
                    _orders.value = res.body()?.orders ?: emptyList()
                } else {
                    _error.value = "Gagal memuat pesanan"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
