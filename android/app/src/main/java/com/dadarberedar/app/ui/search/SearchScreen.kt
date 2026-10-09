package com.dadarberedar.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.model.Outlet
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.remote.ApiService
import com.dadarberedar.app.ui.menu.MenuProductCard
import com.dadarberedar.app.ui.outlet.OutletCard2
import com.dadarberedar.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _outlets = MutableStateFlow<List<Outlet>>(emptyList())
    val outlets: StateFlow<List<Outlet>> = _outlets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.length >= 2) {
            searchJob = viewModelScope.launch {
                _isLoading.value = true
                delay(500)
                try {
                    val res = apiService.search(newQuery)
                    if (res.isSuccessful && res.body() != null) {
                        val data = res.body()!!
                        _products.value = data.products
                        _outlets.value = data.outlets
                    }
                } catch (e: Exception) {
                    // search failed silently
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _products.value = emptyList()
            _outlets.value = emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOutletClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val products by viewModel.products.collectAsState()
    val outlets by viewModel.outlets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Cari menu atau outlet...", color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberLight) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = TextTertiary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { paddingVals ->
        if (query.length < 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Amber.copy(alpha = 0.08f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AmberLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ketik minimal 2 karakter", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Amber)
            }
        } else if (products.isEmpty() && outlets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada hasil untuk \"$query\"", fontSize = 14.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (outlets.isNotEmpty()) {
                    item {
                        Text(
                            "Outlet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(outlets) { outlet ->
                        OutletCard2(outlet = outlet, onClick = { onOutletClick(outlet.id) })
                    }
                }

                if (products.isNotEmpty()) {
                    item {
                        Text(
                            "Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(
                                top = if (outlets.isNotEmpty()) 16.dp else 0.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                    items(products) { product ->
                        MenuProductCard(product = product, onAddToCart = {})
                    }
                }
            }
        }
    }
}
