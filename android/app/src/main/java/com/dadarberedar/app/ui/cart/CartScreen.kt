package com.dadarberedar.app.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.dadarberedar.app.data.model.resolvedImageUrl
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onProceedToCheckout: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val items = cartViewModel.cartItems
    val subtotal = cartViewModel.subtotal
    val tax = cartViewModel.tax
    val total = cartViewModel.total

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Keranjang", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    shadowElevation = 12.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Pembayaran", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    "Rp ${String.format("%,.0f", total)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentOrange
                                )
                            }
                            Button(
                                onClick = onProceedToCheckout,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp)
                            ) {
                                Text(
                                    "Lanjut ke Checkout",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingVals ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(84.dp),
                        shape = CircleShape,
                        color = Amber.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = BurntAmber
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Keranjang kosong",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tambahkan menu favoritmu dulu, ya!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.product.id }) { cartItem ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = cartItem.product.resolvedImageUrl,
                                contentDescription = cartItem.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GrayLight)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    cartItem.product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Rp ${String.format("%,.0f", cartItem.product.price)}",
                                    color = AccentOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Surface(
                                        onClick = { cartViewModel.updateQuantity(cartItem.product.id, -1) },
                                        shape = CircleShape,
                                        color = GrayLight
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Kurangi",
                                            modifier = Modifier
                                                .size(30.dp)
                                                .padding(7.dp),
                                            tint = TextPrimary
                                        )
                                    }
                                    Text(
                                        "${cartItem.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.width(32.dp),
                                        color = TextPrimary
                                    )
                                    Surface(
                                        onClick = { cartViewModel.updateQuantity(cartItem.product.id, 1) },
                                        shape = CircleShape,
                                        color = Amber
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Tambah",
                                            modifier = Modifier
                                                .size(30.dp)
                                                .padding(7.dp),
                                            tint = White
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { cartViewModel.updateQuantity(cartItem.product.id, -cartItem.quantity) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CartPriceRow("Subtotal", subtotal)
                            Spacer(modifier = Modifier.height(6.dp))
                            CartPriceRow("Pajak (10%)", tax)
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = DividerColor
                            )
                            CartPriceRow("Total", total, isBold = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartPriceRow(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isBold) 17.sp else 14.sp,
            color = TextPrimary
        )
        Text(
            "Rp ${String.format("%,.0f", amount)}",
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isBold) 17.sp else 14.sp,
            color = if (isBold) AccentOrange else TextPrimary
        )
    }
}
