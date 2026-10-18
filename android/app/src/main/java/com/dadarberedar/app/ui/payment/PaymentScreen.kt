package com.dadarberedar.app.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.ui.cart.Resource
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onPaid: (String) -> Unit,
    onTrack: (String) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val payState by viewModel.payState.collectAsState()

    LaunchedEffect(payState) {
        if (payState is Resource.Success) {
            val paidOrder = (payState as Resource.Success<Order>).data
            viewModel.resetPayState()
            onPaid(paidOrder.orderNumber)
        }
    }

    val isPaid = order?.paymentStatus == "paid"
    val isCancelled = order?.orderStatus == "cancelled"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            if (order != null && !isPaid && !isCancelled) {
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
                                    "Rp ${String.format("%,.0f", order?.totalAmount ?: 0.0)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentOrange
                                )
                            }
                            Button(
                                onClick = { viewModel.pay() },
                                enabled = payState !is Resource.Loading,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp)
                            ) {
                                if (payState is Resource.Loading) {
                                    CircularProgressIndicator(
                                        color = White,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bayar Sekarang", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(rememberScrollState())
                .background(BackgroundCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                isLoading && order == null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Amber)
                    }
                }

                order == null -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Pesanan tidak ditemukan", color = TextSecondary, fontSize = 14.sp)
                    }
                }

                else -> {
                    val data = order!!

                    if (isPaid) {
                        StatusBanner(
                            container = SuccessGreen,
                            icon = Icons.Default.CheckCircle,
                            title = "Pembayaran Lunas",
                            subtitle = "Terima kasih! Pesananmu segera diproses pegawai."
                        )
                        Button(
                            onClick = { onTrack(data.orderNumber) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lacak Pesanan", fontWeight = FontWeight.SemiBold)
                        }
                    } else if (isCancelled) {
                        StatusBanner(
                            container = ErrorRed,
                            icon = Icons.Default.Cancel,
                            title = "Pesanan Dibatalkan",
                            subtitle = "Pesanan ini sudah dibatalkan dan tidak perlu dibayar."
                        )
                    } else {
                        StatusBanner(
                            container = AccentOrange,
                            icon = Icons.Default.Schedule,
                            title = "Menunggu Pembayaran",
                            subtitle = "Selesaikan pembayaran agar pesanan segera dikonfirmasi."
                        )
                    }

                    // Ringkasan pesanan
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ringkasan Pesanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text(data.orderNumber, fontSize = 11.sp, color = TextTertiary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            data.items?.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${item.productName} x${item.quantity}",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "Rp ${String.format("%,.0f", item.subtotal)}",
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentRow("Subtotal", data.subtotal)
                            Spacer(modifier = Modifier.height(4.dp))
                            PaymentRow("Pajak (10%)", data.taxAmount)
                            if (data.deliveryFee > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                PaymentRow("Biaya Pengiriman", data.deliveryFee)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentRow("Total Pembayaran", data.totalAmount, isBold = true)
                        }
                    }

                    // Metode pembayaran
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text(
                                    paymentLabel(data.paymentMethod),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BurntAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (data.paymentMethod == "qris") {
                                QrisPlaceholder(amount = data.totalAmount)
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Amber.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = BurntAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Instruksi ${paymentLabel(data.paymentMethod)} dikirim setelah pesanan dikonfirmasi.",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Mode demo: tombol Bayar Sekarang menirukan pembayaran sukses via payment gateway.",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (payState is Resource.Error) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = (payState as Resource.Error).message,
                                modifier = Modifier.padding(12.dp),
                                color = ErrorRed,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(container: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = container, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun QrisPlaceholder(amount: Double) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .size(120.dp)
                    .background(GrayLight, RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("QRIS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(
                "Rp ${String.format("%,.0f", amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentOrange
            )
        }
    }
}

@Composable
private fun PaymentRow(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = TextPrimary
        )
        Text(
            "Rp ${String.format("%,.0f", amount)}",
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) AccentOrange else TextPrimary
        )
    }
}

private fun paymentLabel(method: String?): String = when (method) {
    "qris" -> "QRIS"
    "midtrans_gopay" -> "GoPay"
    "midtrans_bca" -> "Transfer BCA"
    "cod" -> "Bayar di Tempat (COD)"
    else -> method?.uppercase() ?: "-"
}
