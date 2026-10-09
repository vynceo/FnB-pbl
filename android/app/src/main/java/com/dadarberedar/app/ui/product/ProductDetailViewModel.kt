package com.dadarberedar.app.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: -1L

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        if (productId <= 0) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val res = apiService.getProductDetail(productId)
                if (res.isSuccessful && res.body()?.product != null) {
                    _product.value = res.body()!!.product
                } else {
                    _error.value = "Menu tidak ditemukan"
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat menu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
