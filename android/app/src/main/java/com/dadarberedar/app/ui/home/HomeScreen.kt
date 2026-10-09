package com.dadarberedar.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dadarberedar.app.R
import com.dadarberedar.app.data.model.Category
import com.dadarberedar.app.data.model.Outlet
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.resolvedImageUrl
import com.dadarberedar.app.ui.cart.CartViewModel
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOutletClick: (Long) -> Unit,
    onProductClick: (Long) -> Unit,
    onCategoryClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val outlets by viewModel.outlets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val bestsellers by viewModel.bestsellers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                title = {
                    Column {
                        Text(
                            "Dadar Beredar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Nasi & Lauk Panas",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = TextPrimary)
                    }
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = AccentOrange) {
                                    Text("$cartCount", color = White, fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Keranjang",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingVals ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Amber)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { HeroBanner(onPesanClick = onMenuClick) }

                if (categories.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Kategori", subtitle = "Lihat Semua", onSeeAll = onMenuClick)
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            categories.take(4).forEach { category ->
                                CategoryTile(
                                    category = category,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.selectCategory(category.id)
                                        onCategoryClick()
                                    }
                                )
                            }
                        }
                    }
                }

                if (bestsellers.isNotEmpty()) {
                    item { SectionHeader(title = "Menu Favorit", subtitle = "Lihat Semua", onSeeAll = onMenuClick) }
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            bestsellers.take(5).forEach { product ->
                                FavoriteMenuRow(
                                    product = product,
                                    onClick = { onProductClick(product.id) },
                                    onAddToCart = {
                                        cartViewModel.addToCart(
                                            product,
                                            outlets.firstOrNull()?.id ?: 1L
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (outlets.isNotEmpty()) {
                    item { SectionHeader(title = "Outlet Terdekat", subtitle = null) }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(outlets.take(5)) { outlet ->
                                OutletCardCompact(outlet = outlet) { onOutletClick(outlet.id) }
                            }
                        }
                    }
                }

                if (error != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(error!!, fontSize = 13.sp, color = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBanner(onPesanClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Amber),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    "Telur Dadar Krispy",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sambal Rempah Legendaris",
                    color = White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = onPesanClick,
                    shape = RoundedCornerShape(22.dp),
                    color = White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Pesan Sekarang",
                            color = BurntAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = BurntAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String?, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
        if (subtitle != null && onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(subtitle, fontSize = 13.sp, color = BurntAmber, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CategoryTile(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val icon = when (category.type) {
        "food" -> Icons.Default.Restaurant
        "beverage" -> Icons.Default.LocalCafe
        "side" -> Icons.Default.LunchDining
        else -> Icons.Default.Fastfood
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(16.dp),
            color = Amber.copy(alpha = 0.14f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = category.name,
                    tint = BurntAmber,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FavoriteMenuRow(product: Product, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    .size(62.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrayLight)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!product.description.isNullOrBlank()) {
                    Text(
                        product.description ?: "",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Rp ${String.format("%,.0f", product.price)}",
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontSize = 14.sp
                )
            }
            Surface(
                onClick = onAddToCart,
                shape = CircleShape,
                color = Amber
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = White,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun OutletCardCompact(outlet: Outlet, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (outlet.isActive) SuccessGreen else ErrorRed,
                    shape = CircleShape,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    outlet.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BurntAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    outlet.city,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${outlet.rating} (${outlet.reviewCount}+)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    outlet.openingHours,
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
