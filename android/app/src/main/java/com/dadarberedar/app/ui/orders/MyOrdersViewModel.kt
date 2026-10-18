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
    private val authManager: AuthManager,
    private val historyStore: OrderHistoryStore
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

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

                val merged = LinkedHashMap<String, Order>()

                if (loggedIn) {
                    val token = authManager.getToken() ?: ""
                    val res = apiService.getMyOrders("Bearer $token")
                    if (res.isSuccessful) {
                        res.body()?.orders?.forEach { merged[it.orderNumber] = it }
                    }
                }

                historyStore.getNumbers().forEach { number ->
                    if (!merged.containsKey(number)) {
                        try {
                            val res = apiService.getOrderTracking(number)
                            val fetched = res.body()?.order
                            if (res.isSuccessful && fetched != null) {
                                merged[number] = fetched
                            }
                        } catch (_: Exception) {
                        }
                    }
                }

                _orders.value = merged.values
                    .sortedByDescending { it.createdAt ?: "" }

                if (!loggedIn && merged.isEmpty()) {
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat riwayat: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelOrder(orderNumber: String) {
        viewModelScope.launch {
            _isWorking.value = true
            _message.value = null
            try {
                val token = authManager.getToken()?.let { "Bearer $it" }
                val res = apiService.cancelOrder(orderNumber, token)
                _message.value = when {
                    res.isSuccessful -> res.body()?.message ?: "Pesanan dibatalkan"
                    else -> parseError(res.errorBody()?.string()) ?: "Gagal membatalkan pesanan"
                }
                if (res.isSuccessful) loadOrders()
            } catch (e: Exception) {
                _message.value = "Gagal membatalkan pesanan"
            } finally {
                _isWorking.value = false
            }
        }
    }

    fun payOrder(orderNumber: String, onPaid: (String) -> Unit) {
        viewModelScope.launch {
            _isWorking.value = true
            _message.value = null
            try {
                val res = apiService.simulatePayment(orderNumber)
                if (res.isSuccessful) {
                    _message.value = res.body()?.message ?: "Pembayaran berhasil"
                    loadOrders()
                    onPaid(orderNumber)
                } else {
                    _message.value = parseError(res.errorBody()?.string()) ?: "Pembayaran gagal"
                }
            } catch (e: Exception) {
                _message.value = "Pembayaran gagal: jaringan bermasalah"
            } finally {
                _isWorking.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseError(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = com.google.gson.JsonParser.parseString(body).asJsonObject
            json.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }
}
