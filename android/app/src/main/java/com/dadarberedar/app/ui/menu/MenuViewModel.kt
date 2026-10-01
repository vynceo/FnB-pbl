package com.dadarberedar.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Category
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allProducts = listOf<Product>()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val catRes = apiService.getCategories()
                if (catRes.isSuccessful) {
                    _categories.value = catRes.body()?.categories ?: emptyList()
                }
                val prodRes = apiService.getProducts()
                if (prodRes.isSuccessful) {
                    allProducts = prodRes.body()?.products ?: emptyList()
                    _products.value = allProducts
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
        _products.value = if (categoryId != null) {
            allProducts.filter { it.categoryId == categoryId }
        } else {
            allProducts
        }
    }
}
