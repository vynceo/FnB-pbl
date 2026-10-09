package com.dadarberedar.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.resolvedImageUrl
import com.dadarberedar.app.ui.cart.CartViewModel
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onSearchClick: () -> Unit,
    onProductClick: (Long) -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    viewModel: MenuViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val query by viewModel.query.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Menu", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = AccentOrange) {
                                    Text("$cartCount", color = White, fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(BackgroundCream)
        ) {
            Surface(color = SurfaceWhite) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Cari menu...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Bersihkan", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val selectedName = categories.find { it.id == selectedCategory }?.name ?: "Semua Kategori"

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = BurntAmber) },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber,
                                unfocusedBorderColor = DividerColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Kategori") },
                                onClick = {
                                    viewModel.selectCategory(null)
                                    dropdownExpanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) }
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.selectCategory(category.id)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Amber)
                }
            } else if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Menu tidak ditemukan", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { product ->
                        MenuProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = { cartViewModel.addToCart(product, 1L) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuProductCard(product: Product, onClick: () -> Unit = {}, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.resolvedImageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrayLight)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (product.isSignature) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AccentOrange,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Signature",
                                fontSize = 9.sp,
                                color = White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (!product.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        product.description ?: "",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rp ${String.format("%,.0f", product.price)}",
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange,
                        fontSize = 15.sp
                    )
                    Surface(
                        onClick = onAddToCart,
                        shape = CircleShape,
                        color = Amber
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(7.dp),
                            tint = White
                        )
                    }
                }
            }
        }
    }
}
