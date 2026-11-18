package com.dadarberedar.app.ui.cart

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.*
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.ui.orders.OrderHistoryStore
import com.dadarberedar.app.data.remote.AuthManager
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
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
    private val authManager: AuthManager,
    private val cartStorage: CartStorage,
    private val historyStore: OrderHistoryStore
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

    suspend fun getSavedUserDetails(): Triple<String?, String?, String?> {
        return Triple(
            authManager.getUserName(),
            authManager.getUserPhone(),
            authManager.getUserEmail()
        )
    }

    fun resetCheckoutState() {
        _checkoutState.value = Resource.Idle
    }

    fun checkoutViaWhatsApp(
        context: Context,
        outletId: Long,
        deliveryType: String,
        customerName: String,
        customerPhone: String,
        day: String,
        date: String,
        time: String,
        deliveryAddress: String?,
        deliveryNotes: String?,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            _checkoutState.value = Resource.Loading
            try {
                val outletRes = apiService.getOutletDetail(outletId)
                val outlet = if (outletRes.isSuccessful) outletRes.body()?.outlet else null
                val rawWhatsApp = outlet?.whatsapp ?: outlet?.phone
                if (rawWhatsApp.isNullOrBlank()) {
                    _checkoutState.value = Resource.Error("Nomor WhatsApp outlet tidak tersedia.")
                    return@launch
                }
                val normalizedPhone = normalizePhoneNumber(rawWhatsApp)
                val outletName = outlet?.name ?: "Dadar Beredar Outlet"

                val deliveryFee = 5000.0
                val finalTotal = total + deliveryFee

                val sb = StringBuilder()
                sb.append("Halo, saya ingin melakukan pemesanan.\n\n")
                sb.append("*DETAIL PESANAN*\n")
                sb.append("Outlet: $outletName\n")
                sb.append("Nama: $customerName\n")
                sb.append("No. HP: $customerPhone\n")
                if (day.isNotBlank() || date.isNotBlank()) {
                    sb.append("Hari/Tanggal: ${day.ifBlank { "-" }}, ${date.ifBlank { "-" }}\n")
                }
                if (time.isNotBlank()) {
                    sb.append("Jam: $time\n")
                }
                if (!deliveryAddress.isNullOrBlank()) {
                    sb.append("Alamat Pengiriman: $deliveryAddress\n")
                }
                sb.append("Metode Pembayaran: $paymentMethod\n\n")

                sb.append("*DAFTAR MENU*\n")
                _cartItems.forEachIndexed { index, item ->
                    val itemTotal = item.product.price * item.quantity
                    sb.append("${index + 1}. ${item.product.name} x${item.quantity}\n")
                    sb.append("   Harga: Rp${String.format("%,.0f", item.product.price)}\n")
                    sb.append("   Total: Rp${String.format("%,.0f", itemTotal)}\n")
                    if (!item.notes.isNullOrBlank()) {
                        sb.append("   Catatan: ${item.notes}\n")
                    }
                    sb.append("\n")
                }

                sb.append("Subtotal: Rp${String.format("%,.0f", subtotal)}\n")
                sb.append("Ongkir: Rp${String.format("%,.0f", deliveryFee)}\n")
                sb.append("*TOTAL: Rp${String.format("%,.0f", finalTotal)}*\n\n")

                if (!deliveryNotes.isNullOrBlank()) {
                    sb.append("Catatan: $deliveryNotes\n\n")
                }

                sb.append("Mohon konfirmasi pesanan saya. Terima kasih.")

                val message = sb.toString()
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val uri = Uri.parse("https://wa.me/$normalizedPhone?text=$encodedMessage")

                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(intent)
                    _checkoutState.value = Resource.Success(
                        Order(
                            id = 0,
                            orderNumber = "WA-${System.currentTimeMillis()}",
                            outletId = outletId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            deliveryType = deliveryType,
                            deliveryAddress = deliveryAddress,
                            deliveryNotes = deliveryNotes,
                            subtotal = subtotal,
                            deliveryFee = deliveryFee,
                            taxAmount = tax,
                            totalAmount = finalTotal,
                            outlet = outlet
                        )
                    )
                } catch (e: Exception) {
                    _checkoutState.value = Resource.Error("WhatsApp tidak terpasang di perangkat.")
                }
            } catch (e: Exception) {
                _checkoutState.value = Resource.Error(e.localizedMessage ?: "Gagal menyusun pesan WhatsApp.")
            }
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return when {
            digits.startsWith("0") -> "62" + digits.substring(1)
            digits.startsWith("62") -> digits
            else -> "62$digits"
        }
    }
}
