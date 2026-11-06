package com.dadarberedar.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.local.dao.CategoryDao
import com.dadarberedar.app.data.local.dao.OutletDao
import com.dadarberedar.app.data.local.dao.ProductDao
import com.dadarberedar.app.data.local.entity.toEntity
import com.dadarberedar.app.data.local.entity.toModel
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
    private val selectionBus: MenuSelectionBus,
    private val outletDao: OutletDao,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao
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

            // 1. Load from local cache first (offline-first capability)
            try {
                val cachedOutlets = outletDao.getAllOutlets().map { it.toModel() }
                val cachedCategories = categoryDao.getAllCategories().map { it.toModel() }
                val cachedBestsellers = productDao.getBestsellerProducts().map { it.toModel() }
                val cachedProducts = productDao.getAllProducts().map { it.toModel() }

                if (cachedOutlets.isNotEmpty()) _outlets.value = cachedOutlets
                if (cachedCategories.isNotEmpty()) _categories.value = cachedCategories
                if (cachedBestsellers.isNotEmpty()) _bestsellers.value = cachedBestsellers
                if (cachedProducts.isNotEmpty()) _products.value = cachedProducts
            } catch (_: Exception) {}

            // 2. Fetch fresh data from network and update cache
            try {
                val outletRes = apiService.getOutlets()
                if (outletRes.isSuccessful) {
                    val list = outletRes.body()?.outlets ?: emptyList()
                    _outlets.value = list
                    outletDao.insertOutlets(list.map { it.toEntity() })
                }

                val catRes = apiService.getCategories()
                if (catRes.isSuccessful) {
                    val list = catRes.body()?.categories ?: emptyList()
                    _categories.value = list
                    categoryDao.insertCategories(list.map { it.toEntity() })
                }

                val bestRes = apiService.getProducts(bestseller = true)
                if (bestRes.isSuccessful) {
                    val list = bestRes.body()?.products ?: emptyList()
                    _bestsellers.value = list
                    productDao.insertProducts(list.map { it.toEntity() })
                }

                val prodRes = apiService.getProducts()
                if (prodRes.isSuccessful) {
                    val list = prodRes.body()?.products ?: emptyList()
                    _products.value = list
                    productDao.insertProducts(list.map { it.toEntity() })
                }
            } catch (e: Exception) {
                if (_outlets.value.isEmpty() && _categories.value.isEmpty()) {
                    _error.value = "Gagal memuat data: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
