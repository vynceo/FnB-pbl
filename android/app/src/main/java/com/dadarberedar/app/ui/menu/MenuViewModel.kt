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
    private val apiService: ApiService,
    private val selectionBus: MenuSelectionBus
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allProducts = listOf<Product>()

    init {
        viewModelScope.launch {
            selectionBus.categoryId.collect { id ->
                _selectedCategory.value = id
                applyFilter()
            }
        }
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
                    applyFilter()
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
        selectionBus.select(categoryId)
        applyFilter()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        applyFilter()
    }

    private fun applyFilter() {
        val categoryId = _selectedCategory.value
        val q = _query.value.trim()
        _products.value = allProducts
            .asSequence()
            .filter { categoryId == null || it.categoryId == categoryId }
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) || it.description?.contains(q, true) == true }
            .toList()
    }
}
