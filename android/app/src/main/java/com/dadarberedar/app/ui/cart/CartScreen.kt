package com.dadarberedar.app.ui.cart

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
                    Text("Keranjang", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CartPriceRow("Subtotal", subtotal)
                        CartPriceRow("Pajak (10%)", tax)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = DividerColor
                        )
                        CartPriceRow("Total", total, isBold = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onProceedToCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Checkout (${items.size} item)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = PrimaryBrown.copy(alpha = 0.08f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = PrimaryBrownLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Keranjang kosong", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tambahkan menu dari outlet terdekat",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.product.id }) { cartItem ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    cartItem.product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Rp ${String.format("%,.0f", cartItem.product.price)}",
                                    color = PrimaryBrown,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (cartItem.product.spicinessLevel > 0) {
                                    Row(modifier = Modifier.padding(top = 2.dp)) {
                                        repeat(cartItem.product.spicinessLevel) {
                                            Text("🌶", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    onClick = { cartViewModel.updateQuantity(cartItem.product.id, -1) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = GrayLight
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Kurangi",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(7.dp),
                                        tint = TextPrimary
                                    )
                                }
                                Text(
                                    "${cartItem.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.width(32.dp),
                                    color = TextPrimary
                                )
                                Surface(
                                    onClick = { cartViewModel.updateQuantity(cartItem.product.id, 1) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryBrown
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
            fontSize = if (isBold) 18.sp else 14.sp,
            color = TextPrimary
        )
        Text(
            "Rp ${String.format("%,.0f", amount)}",
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isBold) 18.sp else 14.sp,
            color = if (isBold) PrimaryBrown else TextPrimary
        )
    }
}
