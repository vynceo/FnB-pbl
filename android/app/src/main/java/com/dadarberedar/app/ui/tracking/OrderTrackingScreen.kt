package com.dadarberedar.app.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    onBack: () -> Unit,
    viewModel: OrderTrackingViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lacak Pesanan", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(rememberScrollState())
                .background(BackgroundCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading && order == null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBrown)
                }
            } else if (order != null) {
                val data = order!!

                // Order info card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = PrimaryBrown,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pesanan #${data.orderNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }

                        HorizontalDivider(color = DividerColor)

                        TrackingStatusRow("Status Pesanan", data.orderStatus)
                        TrackingStatusRow("Status Pembayaran", data.paymentStatus)
                        TrackingStatusRow(
                            "Jenis Pesanan",
                            data.deliveryType.replace("_", " ").replaceFirstChar { it.uppercase() }
                        )
                        data.paymentMethod?.let {
                            TrackingStatusRow("Metode Bayar", it.uppercase())
                        }
                        TrackingStatusRow("Total", "Rp ${String.format("%,.0f", data.totalAmount)}")

                        if (!data.items.isNullOrEmpty()) {
                            HorizontalDivider(color = DividerColor)
                            Text(
                                "Item Pesanan:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            data.items.forEach { item ->
                                Text(
                                    "• ${item.productName} x${item.quantity} - Rp ${String.format("%,.0f", item.subtotal)}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Simulation buttons
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Simulasi (Demo)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryBrown
                        )

                        Button(
                            onClick = { viewModel.simulatePayment() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulasi Pembayaran Lunas")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("cooking" to "Memasak", "ready" to "Siap", "delivered" to "Selesai").forEach { (status, label) ->
                                OutlinedButton(
                                    onClick = { viewModel.simulateStatus(status) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (!statusMsg.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryBrown.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = statusMsg!!,
                            modifier = Modifier.padding(12.dp),
                            color = PrimaryBrown,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Pesanan tidak ditemukan", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun TrackingStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
