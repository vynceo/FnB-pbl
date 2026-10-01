package com.dadarberedar.app.ui.outlet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Outlet
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.Review
import com.dadarberedar.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutletDetailViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val outletId: Long = savedStateHandle.get<Long>("outletId") ?: 1L

    private val _outlet = MutableStateFlow<Outlet?>(null)
    val outlet: StateFlow<Outlet?> = _outlet.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val outletRes = apiService.getOutletDetail(outletId)
                if (outletRes.isSuccessful) {
                    _outlet.value = outletRes.body()?.outlet
                }

                val prodRes = apiService.getOutletProducts(outletId)
                if (prodRes.isSuccessful) {
                    _products.value = prodRes.body()?.products ?: emptyList()
                }

                val revRes = apiService.getOutletReviews(outletId)
                if (revRes.isSuccessful) {
                    _reviews.value = revRes.body()?.reviews ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat detail: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun getFilteredProducts(): List<Product> {
        val catId = _selectedCategoryId.value
        return if (catId != null) {
            _products.value.filter { it.categoryId == catId }
        } else {
            _products.value
        }
    }
}
