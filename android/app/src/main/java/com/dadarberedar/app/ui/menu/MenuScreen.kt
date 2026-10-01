package com.dadarberedar.app.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.dadarberedar.app.data.model.Category
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.resolvedImageUrl
import com.dadarberedar.app.ui.cart.CartViewModel
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onSearchClick: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    viewModel: MenuViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Menu", fontWeight = FontWeight.Bold)
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = AccentGold) {
                                    Text("$cartCount", color = White)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = White,
                    actionIconContentColor = White
                )
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(BackgroundCream)
        ) {
            // Category chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBrown,
                            selectedLabelColor = White
                        )
                    )
                }
                items(categories) { category ->
                    val icon = when (category.type) {
                        "food" -> Icons.Default.Restaurant
                        "beverage" -> Icons.Default.LocalCafe
                        "side" -> Icons.Default.LunchDining
                        else -> Icons.Default.Fastfood
                    }
                    FilterChip(
                        selected = selectedCategory == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = { Text(category.name, maxLines = 1) },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBrown,
                            selectedLabelColor = White,
                            selectedLeadingIconColor = White
                        )
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBrown)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(products) { product ->
                        MenuProductCard(
                            product = product,
                            onAddToCart = { cartViewModel.addToCart(product, 1L) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.resolvedImageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                            color = AccentGold,
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
                if (product.description.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        product.description ?: "",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.isBestseller) {
                        Surface(
                            color = AccentGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Bestseller",
                                fontSize = 10.sp,
                                color = AccentOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (product.isCrispy) {
                        Surface(
                            color = PrimaryBrownLight.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Krispy",
                                fontSize = 10.sp,
                                color = PrimaryBrown,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (product.spicinessLevel > 0) {
                        repeat(product.spicinessLevel) {
                            Text("🌶", fontSize = 10.sp)
                        }
                    }
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
                        color = PrimaryBrown,
                        fontSize = 15.sp
                    )
                    Surface(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBrown
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp, color = White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
