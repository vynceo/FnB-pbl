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
    val quantity: Int,
    val notes: String? = null
)

sealed class Resource<out T> {
    object Idle : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val apiService: ApiService,
    private val cartStorage: CartStorage
) : ViewModel() {

    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    var selectedOutletId = mutableStateOf<Long?>(null)

    init {
        viewModelScope.launch {
            val (items, outletId) = cartStorage.load()
            if (items.isNotEmpty()) {
                _cartItems.addAll(items)
                selectedOutletId.value = outletId
            }
        }
    }

    private fun persist() {
        viewModelScope.launch {
            cartStorage.save(_cartItems.toList(), selectedOutletId.value)
        }
    }

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

    fun addToCart(product: Product, outletId: Long, quantity: Int = 1) {
        if (selectedOutletId.value != null && selectedOutletId.value != outletId) {
            _cartItems.clear()
        }
        selectedOutletId.value = outletId

        val index = _cartItems.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val current = _cartItems[index]
            _cartItems[index] = current.copy(quantity = current.quantity + quantity)
        } else {
            _cartItems.add(CartItem(product = product, quantity = quantity))
        }
        persist()
    }

    fun updateQuantity(productId: Long, delta: Int) {
        val index = _cartItems.indexOfFirst { it.product.id == productId }
        if (index < 0) return
        val current = _cartItems[index]
        val newQuantity = current.quantity + delta
        if (newQuantity <= 0) {
            _cartItems.removeAt(index)
            if (_cartItems.isEmpty()) {
                selectedOutletId.value = null
            }
        } else {
            _cartItems[index] = current.copy(quantity = newQuantity)
        }
        persist()
    }

    fun updateNotes(productId: Long, notes: String) {
        val index = _cartItems.indexOfFirst { it.product.id == productId }
        if (index < 0) return
        _cartItems[index] = _cartItems[index].copy(notes = notes.ifBlank { null })
        persist()
    }

    fun clearCart() {
        _cartItems.clear()
        selectedOutletId.value = null
        viewModelScope.launch { cartStorage.clear() }
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
