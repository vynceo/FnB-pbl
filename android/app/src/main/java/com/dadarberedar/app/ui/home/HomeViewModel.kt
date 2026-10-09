package com.dadarberedar.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Category
import com.dadarberedar.app.data.model.Outlet
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.ui.menu.MenuSelectionBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val selectionBus: MenuSelectionBus
) : ViewModel() {

    private val _outlets = MutableStateFlow<List<Outlet>>(emptyList())
    val outlets: StateFlow<List<Outlet>> = _outlets.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _bestsellers = MutableStateFlow<List<Product>>(emptyList())
    val bestsellers: StateFlow<List<Product>> = _bestsellers.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadHomeData()
    }

    fun selectCategory(categoryId: Long?) {
        selectionBus.select(categoryId)
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val outletRes = apiService.getOutlets()
                if (outletRes.isSuccessful) {
                    _outlets.value = outletRes.body()?.outlets ?: emptyList()
                }

                val catRes = apiService.getCategories()
                if (catRes.isSuccessful) {
                    _categories.value = catRes.body()?.categories ?: emptyList()
                }

                val bestRes = apiService.getProducts(bestseller = true)
                if (bestRes.isSuccessful) {
                    _bestsellers.value = bestRes.body()?.products ?: emptyList()
                }

                val prodRes = apiService.getProducts()
                if (prodRes.isSuccessful) {
                    _products.value = prodRes.body()?.products ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat data: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
