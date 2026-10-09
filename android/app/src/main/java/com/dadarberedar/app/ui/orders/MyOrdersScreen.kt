package com.dadarberedar.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: MyOrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pesanan Saya", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingVals ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Amber)
            }
        } else if (!isLoggedIn) {
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
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AmberLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Silakan masuk", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Masuk untuk melihat riwayat pesanan", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else if (orders.isEmpty()) {
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
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AmberLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada pesanan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pesan sekarang dan nikmati lezatnya dadar!", fontSize = 13.sp, color = TextSecondary)
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
                items(orders) { order ->
                    OrderCard(order = order, onClick = { onOrderClick(order.orderNumber) })
                }
            }
        }

        if (error != null && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                Text(error!!, color = ErrorRed, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val (statusColor, statusLabel) = when (order.orderStatus) {
        "received" -> Amber to "Diterima"
        "cooking" -> AccentGold to "Sedang Dimasak"
        "ready" -> SuccessGreen to "Siap Diambil"
        "delivered" -> SuccessGreen to "Selesai"
        "cancelled" -> ErrorRed to "Dibatalkan"
        else -> TextSecondary to order.orderStatus
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.deliveryType.replace("_", " ").replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    "Rp ${String.format("%,.0f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Amber
                )
            }
            order.createdAt?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, fontSize = 11.sp, color = TextTertiary)
            }
        }
    }
}
