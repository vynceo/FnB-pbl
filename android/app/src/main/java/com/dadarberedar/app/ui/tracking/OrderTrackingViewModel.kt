package com.dadarberedar.app.ui.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderNumber: String = savedStateHandle.get<String>("orderNumber") ?: ""

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (orderNumber.isNotEmpty()) {
            fetchOrder()
        }
    }

    fun fetchOrder() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = apiService.getOrderTracking(orderNumber)
                if (res.isSuccessful && res.body()?.order != null) {
                    _order.value = res.body()!!.order
                } else {
                    _statusMessage.value = "Gagal memuat data pesanan"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun simulatePayment() {
        viewModelScope.launch {
            try {
                val res = apiService.simulatePayment(orderNumber)
                if (res.isSuccessful) {
                    _statusMessage.value = "Pembayaran berhasil disimulasi!"
                    fetchOrder()
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
                    fetchOrder()
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal mengubah status"
            }
        }
    }
}
