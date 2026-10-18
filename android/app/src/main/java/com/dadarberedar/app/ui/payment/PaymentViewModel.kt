package com.dadarberedar.app.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.ui.cart.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderNumber: String = savedStateHandle.get<String>("orderNumber") ?: ""

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _payState = MutableStateFlow<Resource<Order>>(Resource.Idle)
    val payState: StateFlow<Resource<Order>> = _payState.asStateFlow()

    init {
        fetchOrder()
    }

    fun fetchOrder() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = apiService.getOrderTracking(orderNumber)
                if (res.isSuccessful && res.body()?.order != null) {
                    _order.value = res.body()!!.order
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pay() {
        viewModelScope.launch {
            _payState.value = Resource.Loading
            try {
                val res = apiService.simulatePayment(orderNumber)
                if (res.isSuccessful && res.body()?.order != null) {
                    _order.value = res.body()!!.order
                    _payState.value = Resource.Success(res.body()!!.order!!)
                } else {
                    val msg = res.errorBody()?.string()
                        ?.let { runCatching { com.google.gson.JsonParser.parseString(it).asJsonObject.get("message")?.asString }.getOrNull() }
                    _payState.value = Resource.Error(msg ?: "Pembayaran gagal, coba lagi")
                }
            } catch (e: Exception) {
                _payState.value = Resource.Error("Jaringan bermasalah, coba lagi")
            }
        }
    }

    fun resetPayState() {
        _payState.value = Resource.Idle
    }
}
