package com.dadarberedar.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onOrderClick: (String) -> Unit,
    onPayClick: (String) -> Unit,
    onLoginClick: () -> Unit = {},
    viewModel: MyOrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val message by viewModel.message.collectAsState()
    val isWorking by viewModel.isWorking.collectAsState()

    var filter by remember { mutableStateOf("all") }
    var cancelTarget by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            delay(3000)
            viewModel.clearMessage()
        }
    }

    val filteredOrders = remember(orders, filter) {
        when (filter) {
            "unpaid" -> orders.filter {
                it.paymentStatus == "pending" && it.orderStatus != "cancelled"
            }
            "ongoing" -> orders.filter {
                it.orderStatus in listOf("received", "cooking", "ready") &&
                    it.paymentStatus == "paid"
            }
            "done" -> orders.filter { it.orderStatus == "delivered" }
            "cancelled" -> orders.filter { it.orderStatus == "cancelled" }
            else -> orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Riwayat Pesanan", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }, enabled = !isLoading && !isWorking) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingVals ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .background(BackgroundCream)
            ) {
            if (!isLoggedIn && !isLoading) {
                Surface(color = Amber.copy(alpha = 0.14f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BurntAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Masuk agar riwayat tersinkron di semua perangkat",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onLoginClick) {
                            Text("Masuk", fontSize = 12.sp, color = BurntAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        "all" to "Semua",
                        "unpaid" to "Belum Bayar",
                        "ongoing" to "Berjalan",
                        "done" to "Selesai",
                        "cancelled" to "Dibatalkan"
                    )
                ) { (value, label) ->
                    FilterChip(
                        selected = filter == value,
                        onClick = { filter = value },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            when {
                isLoading && orders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Amber)
                    }
                }

                error != null && orders.isEmpty() && isLoggedIn -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error!!, color = ErrorRed, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = { viewModel.loadOrders() }) { Text("Coba Lagi") }
                        }
                    }
                }

                orders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(84.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = Amber.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp),
                                        tint = BurntAmber
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Belum ada pesanan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isLoggedIn) "Pesan sekarang dan nikmati dadar favoritmu!"
                                else "Pesananmu sebagai tamu juga muncul di sini",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                filteredOrders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada pesanan pada filter ini", fontSize = 14.sp, color = TextSecondary)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredOrders, key = { it.orderNumber }) { order ->
                            HistoryOrderCard(
                                order = order,
                                isWorking = isWorking,
                                onTrack = { onOrderClick(order.orderNumber) },
                                onPay = { onPayClick(order.orderNumber) },
                                onCancel = { cancelTarget = order }
                            )
                        }
                    }
                }
            }
        }

        message?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = TextPrimary,
                contentColor = White
            ) {
                Text(msg, fontSize = 13.sp)
            }
        }
        }
    }

    cancelTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text("Batalkan Pesanan?") },
            text = {
                Text("Pesanan #${target.orderNumber} akan dibatalkan. Pembatalan hanya bisa dilakukan sebelum pegawai mengonfirmasi (status Diterima).")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(target.orderNumber)
                        cancelTarget = null
                    }
                ) {
                    Text("Ya, Batalkan", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Kembali") }
            }
        )
    }
}

@Composable
private fun HistoryOrderCard(
    order: Order,
    isWorking: Boolean,
    onTrack: () -> Unit,
    onPay: () -> Unit,
    onCancel: () -> Unit
) {
    val (statusColor, statusLabel) = when (order.orderStatus) {
        "received" -> Amber to "Menunggu Konfirmasi"
        "cooking" -> AccentOrange to "Sedang Dimasak"
        "ready" -> SuccessGreen to "Siap Diambil"
        "delivered" -> SuccessGreen to "Selesai"
        "cancelled" -> ErrorRed to "Dibatalkan"
        else -> TextSecondary to order.orderStatus
    }
    val unpaid = order.paymentStatus == "pending" && order.orderStatus != "cancelled"
    val canCancel = order.orderStatus == "received"

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Surface(color = statusColor, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (unpaid) Icons.Default.Schedule else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (unpaid) AccentOrange else SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (unpaid) "Belum Bayar" else if (order.paymentStatus == "paid") "Sudah Lunas" else order.paymentStatus,
                    fontSize = 12.sp,
                    color = if (unpaid) AccentOrange else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${order.items?.size ?: 0} item • ${order.deliveryType.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            order.createdAt?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it.take(16).replace('T', ' '), fontSize = 11.sp, color = TextTertiary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", fontSize = 13.sp, color = TextSecondary)
                Text(
                    "Rp ${String.format("%,.0f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTrack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lacak", fontSize = 12.sp)
                }

                if (unpaid) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.weight(1f),
                        enabled = !isWorking,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bayar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (canCancel) {
                    TextButton(
                        onClick = onCancel,
                        enabled = !isWorking,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Batalkan", fontSize = 12.sp, color = ErrorRed)
                    }
                }
            }
        }
    }
}
