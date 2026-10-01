package com.dadarberedar.app.ui.home

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val outlets by viewModel.outlets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val bestsellers by viewModel.bestsellers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dadar Beredar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = White
                        )
                        Text(
                            "Telur Dadar Krispy, Sambal Rempah",
                            fontSize = 11.sp,
                            color = White.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrownDark
                )
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
                CircularProgressIndicator(color = PrimaryBrown)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Hero Banner
                item {
                    HeroBanner()
                }

                // Promo Card
                item {
                    PromoCard()
                }

                // Quick Categories
                if (categories.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Kategori",
                            subtitle = "Lihat Semua",
                            onSeeAll = onMenuClick
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories.take(8)) { category ->
                                CategoryItem(category) { onMenuClick() }
                            }
                        }
                    }
                }

                // Bestsellers
                if (bestsellers.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Bestseller", subtitle = null)
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bestsellers) { product ->
                                BestsellerCard(
                                    product = product,
                                    onAddToCart = { cartViewModel.addToCart(product, outlets.firstOrNull()?.id ?: 1L) }
                                )
                            }
                        }
                    }
                }

                // Outlets
                if (outlets.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Outlet Terdekat", subtitle = null)
                    }
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

                // Error
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
                                Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
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
fun HeroBanner() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(PrimaryBrownDark, PrimaryBrown)
    )
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column {
                Surface(
                    color = AccentGold,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "FRANCHISE BY BABE CABITA & KING ABDI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Telur Dadar Krispy",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sambal Rempah Legendaris",
                    color = AccentGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Gratis ongkir untuk takeaway!",
                            fontSize = 12.sp,
                            color = White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = AccentGold,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Promo Spesial!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBrownDark)
                Text("Pesan 3 telur dadar gratis 1 sambal rempah", fontSize = 12.sp, color = TextSecondary)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryBrownLight
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String?, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        if (subtitle != null && onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(subtitle, fontSize = 13.sp, color = PrimaryBrown)
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    val icon = when (category.type) {
        "food" -> Icons.Default.Restaurant
        "beverage" -> Icons.Default.LocalCafe
        "side" -> Icons.Default.LunchDining
        else -> Icons.Default.Fastfood
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryBrown.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = category.name,
                    tint = PrimaryBrown,
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
fun BestsellerCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(170.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.resolvedImageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                Surface(
                    color = AccentGold,
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 12.dp)
                ) {
                    Text(
                        "Bestseller",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (product.spicinessLevel > 0) {
                    Row {
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
                        fontSize = 13.sp
                    )
                    Surface(
                        onClick = onAddToCart,
                        shape = CircleShape,
                        color = PrimaryBrown
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = White,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(6.dp)
                        )
                    }
                }
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
                    tint = PrimaryBrownLight,
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
