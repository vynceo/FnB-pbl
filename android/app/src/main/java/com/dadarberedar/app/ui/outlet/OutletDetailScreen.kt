package com.dadarberedar.app.ui.outlet

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dadarberedar.app.data.model.Outlet
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.Review
import com.dadarberedar.app.ui.cart.CartViewModel
import com.dadarberedar.app.ui.menu.MenuProductCard
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletDetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: OutletDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val outlet by viewModel.outlet.collectAsState()
    val products by viewModel.products.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val context = LocalContext.current
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(outlet?.name ?: "Detail Outlet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = White)
                    }
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
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang", tint = White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = White,
                    navigationIconContentColor = White,
                    actionIconContentColor = White
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
                // Outlet Info
                item {
                    outlet?.let { o ->
                        Card(
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (o.isActive) SuccessGreen else ErrorRed,
                                        shape = CircleShape,
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(o.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Rating
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${o.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(" (${o.reviewCount}+ ulasan)", fontSize = 13.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                // Hours
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryBrown, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(o.openingHours, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                // Address
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBrown, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${o.address}, ${o.city}", fontSize = 13.sp)
                                }

                                // Phone
                                if (!o.phone.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${o.phone}")))
                                        }
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBrown, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(o.phone, fontSize = 13.sp, color = PrimaryBrown)
                                    }
                                }

                                // WhatsApp
                                if (!o.whatsapp.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val url = "https://wa.me/${o.whatsapp.replace("[^0-9]".toRegex(), "")}"
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    ) {
                                        Text("💬", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat WhatsApp", fontSize = 13.sp, color = PrimaryBrown)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Delivery platform links
                                Text("Pesan via:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    o.gofoodUrl?.let { url ->
                                        DeliveryPlatformButton("GoFood", AccentOrange) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    }
                                    o.grabfoodUrl?.let { url ->
                                        DeliveryPlatformButton("GrabFood", SuccessGreen) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    }
                                    o.shopeefoodUrl?.let { url ->
                                        DeliveryPlatformButton("ShopeeFood", AccentGold) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    }
                                }

                                // Google Maps
                                o.gmapsUrl?.let { url ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryBrown, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Buka di Google Maps", fontSize = 13.sp, color = PrimaryBrown)
                                    }
                                }
                            }
                        }
                    }
                }

                // Category filter chips
                if (products.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryId == null,
                                    onClick = { viewModel.filterByCategory(null) },
                                    label = { Text("Semua") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBrown,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                            val categoryIds = products.map { it.categoryId }.distinct()
                            items(categoryIds) { catId ->
                                val catName = products.firstOrNull { it.categoryId == catId }?.category?.name ?: "Menu"
                                FilterChip(
                                    selected = selectedCategoryId == catId,
                                    onClick = { viewModel.filterByCategory(catId) },
                                    label = { Text(catName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBrown,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Menu (${viewModel.getFilteredProducts().size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = TextPrimary
                        )
                    }

                    items(viewModel.getFilteredProducts()) { product ->
                        MenuProductCard(
                            product = product,
                            onAddToCart = {
                                outlet?.let { o -> cartViewModel.addToCart(product, o.id) }
                            }
                        )
                    }
                }

                // Reviews
                if (reviews.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ulasan (${reviews.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = TextPrimary
                        )
                    }
                    items(reviews) { review ->
                        ReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryPlatformButton(name: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(name, fontSize = 12.sp, color = White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = PrimaryBrown.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = review.authorName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryBrown
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(review.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (review.verifiedBuyer) {
                            Text("Pembeli Terverifikasi", fontSize = 10.sp, color = PrimaryBrown)
                        }
                    }
                }
                Text(review.relativeTime, fontSize = 11.sp, color = TextTertiary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                repeat(5) { i ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < review.rating.toInt()) AccentGold else GrayLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(review.comment, fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun OutletCard2(outlet: Outlet, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (outlet.isActive) SuccessGreen else ErrorRed,
                shape = CircleShape,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(outlet.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBrownLight, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${outlet.address}, ${outlet.city}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${outlet.rating} (${outlet.reviewCount}+)", fontSize = 11.sp, color = TextSecondary)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
        }
    }
}
