package com.dadarberedar.app.ui.cart

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.*
import com.dadarberedar.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: Product,
    var quantity: Int,
    var notes: String? = null
)

sealed class Resource<out T> {
    object Idle : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    var selectedOutletId = mutableStateOf<Long?>(null)

    val subtotal: Double
        get() = _cartItems.sumOf { it.product.price * it.quantity }

    val tax: Double
        get() = subtotal * 0.1

    val deliveryFee: Double
        get() = 0.0

    val total: Double
        get() = subtotal + tax + deliveryFee

    private val _checkoutState = MutableStateFlow<Resource<Order>>(Resource.Idle)
    val checkoutState: StateFlow<Resource<Order>> = _checkoutState.asStateFlow()

    fun addToCart(product: Product, outletId: Long) {
        if (selectedOutletId.value != null && selectedOutletId.value != outletId) {
            _cartItems.clear()
        }
        selectedOutletId.value = outletId

        val existing = _cartItems.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity++
        } else {
            _cartItems.add(CartItem(product = product, quantity = 1))
        }
    }

    fun updateQuantity(productId: Long, delta: Int) {
        val item = _cartItems.find { it.product.id == productId } ?: return
        item.quantity += delta
        if (item.quantity <= 0) {
            _cartItems.remove(item)
            if (_cartItems.isEmpty()) {
                selectedOutletId.value = null
            }
        }
    }

    fun updateNotes(productId: Long, notes: String) {
        val item = _cartItems.find { it.product.id == productId } ?: return
        item.notes = notes.ifBlank { null }
    }

    fun clearCart() {
        _cartItems.clear()
        selectedOutletId.value = null
    }

    fun checkout(
        token: String?,
        outletId: Long,
        deliveryType: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String?,
        deliveryAddress: String?,
        deliveryNotes: String?,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            _checkoutState.value = Resource.Loading
            try {
                val items = _cartItems.map {
                    CheckoutItem(
                        productId = it.product.id,
                        quantity = it.quantity,
                        notes = it.notes
                    )
                }
                val request = CheckoutRequest(
                    outletId = outletId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = customerEmail,
                    deliveryType = deliveryType,
                    deliveryAddress = deliveryAddress,
                    deliveryNotes = deliveryNotes,
                    paymentMethod = paymentMethod,
                    items = items
                )
                val authHeader = token?.let { "Bearer $it" }
                val response = apiService.createOrder(authHeader, request)
                if (response.isSuccessful && response.body()?.order != null) {
                    val order = response.body()!!.order!!
                    clearCart()
                    _checkoutState.value = Resource.Success(order)
                } else {
                    val errorBody = response.errorBody()?.string()
                        ?: response.body()?.message
                        ?: ""
                    _checkoutState.value = Resource.Error("Gagal membuat pesanan. $errorBody")
                }
            } catch (e: Exception) {
                _checkoutState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan.")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = Resource.Idle
    }
}
