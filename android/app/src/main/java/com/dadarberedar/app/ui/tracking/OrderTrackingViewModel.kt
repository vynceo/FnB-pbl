package com.dadarberedar.app.ui.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.data.model.OrderUpdateRequest
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.data.remote.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderNumber: String = savedStateHandle.get<String>("orderNumber") ?: ""

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        if (orderNumber.isNotEmpty()) {
            fetchOrder()
            startPolling()
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    private suspend fun authHeader(): String? =
        authManager.getToken()?.let { "Bearer $it" }

    fun fetchOrder() {
        viewModelScope.launch {
            _isLoading.value = true
            loadOrder(silent = false)
        }
    }

    private suspend fun loadOrder(silent: Boolean) {
        try {
            val res = apiService.getOrderTracking(orderNumber)
            if (res.isSuccessful && res.body()?.order != null) {
                _order.value = res.body()!!.order
            } else if (!silent) {
                _statusMessage.value = "Gagal memuat data pesanan"
            }
        } catch (e: Exception) {
            if (!silent) _statusMessage.value = "Error: ${e.localizedMessage}"
        } finally {
            if (!silent) _isLoading.value = false
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                val current = _order.value?.orderStatus
                if (current == "delivered" || current == "cancelled") break
                loadOrder(silent = true)
            }
        }
    }

    fun cancelOrder() {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val res = apiService.cancelOrder(orderNumber, authHeader())
                if (res.isSuccessful) {
                    _statusMessage.value = res.body()?.message ?: "Pesanan dibatalkan"
                    loadOrder(silent = true)
                } else {
                    _statusMessage.value = "Gagal membatalkan pesanan"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal membatalkan pesanan"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val res = apiService.updateOrder(
                    orderNumber = orderNumber,
                    token = authHeader(),
                    request = OrderUpdateRequest(
                        customerName = customerName.ifBlank { null },
                        customerPhone = customerPhone.ifBlank { null },
                        deliveryAddress = deliveryAddress.ifBlank { null },
                        deliveryNotes = deliveryNotes.ifBlank { null }
                    )
                )
                if (res.isSuccessful) {
                    _statusMessage.value = res.body()?.message ?: "Pesanan diperbarui"
                    loadOrder(silent = true)
                } else {
                    _statusMessage.value = "Gagal memperbarui pesanan"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memperbarui pesanan"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun simulatePayment() {
        viewModelScope.launch {
            try {
                val res = apiService.simulatePayment(orderNumber)
                if (res.isSuccessful) {
                    _statusMessage.value = "Pembayaran berhasil disimulasi!"
                    loadOrder(silent = true)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal simulasi pembayaran"
            }
        }
    }

    fun simulateStatus(newStatus: String) {
        viewModelScope.launch {
            try {
                val res = apiService.simulateStatus(orderNumber, mapOf("order_status" to newStatus))
                if (res.isSuccessful) {
                    _statusMessage.value = "Status diubah ke $newStatus"
                    loadOrder(silent = true)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal mengubah status"
            }
        }
    }

    fun clearMessage() {
        _statusMessage.value = null
    }
}
