package com.dadarberedar.app.ui.tracking

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    onBack: () -> Unit,
    onEditSuccess: () -> Unit = {},
    viewModel: OrderTrackingViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMsg) {
        if (statusMsg != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lacak Pesanan", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                        Text(
                            "Diperbarui otomatis tiap 5 detik",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
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
                    CircularProgressIndicator(color = Amber)
                }
            } else if (order != null) {
                val data = order!!

                Card(
                    shape = RoundedCornerShape(14.dp),
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
                                color = Amber,
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
                                fontSize = 16.sp
                            )
                        }

                        StatusChip(status = data.orderStatus)

                        HorizontalDivider(color = DividerColor)

                        TrackingStatusRow("Status Pembayaran", data.paymentStatus)
                        TrackingStatusRow(
                            "Jenis Pesanan",
                            data.deliveryType.replace("_", " ").replaceFirstChar { it.uppercase() }
                        )
                        data.deliveryAddress?.let {
                            TrackingStatusRow("Alamat", it)
                        }
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

                if (data.orderStatus == "received") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isUpdating
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah Pesanan", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isUpdating,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Batalkan", fontSize = 13.sp)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Simulasi (Demo)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BurntAmber
                        )

                        Button(
                            onClick = { viewModel.simulatePayment() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulasi Pembayaran Lunas")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "cooking" to "Memasak",
                                "ready" to "Siap",
                                "delivered" to "Selesai"
                            ).forEach { (status, label) ->
                                OutlinedButton(
                                    onClick = { viewModel.simulateStatus(status) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(label, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (!statusMsg.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = statusMsg!!,
                            modifier = Modifier.padding(12.dp),
                            color = White,
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

    if (showEditDialog && order != null) {
        EditOrderDialog(
            initialAddress = order?.deliveryAddress ?: "",
            initialNotes = order?.deliveryNotes ?: "",
            initialName = order?.customerName ?: "",
            initialPhone = order?.customerPhone ?: "",
            isUpdating = isUpdating,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, phone, address, notes ->
                viewModel.updateOrder(name, phone, address, notes)
                showEditDialog = false
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pesanan?") },
            text = { Text("Pesanan #${order?.orderNumber} akan dibatalkan dan tidak bisa diproses lagi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder()
                        showCancelDialog = false
                    }
                ) {
                    Text("Ya, Batalkan", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Kembali")
                }
            }
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, container) = when (status) {
        "received" -> "Diterima" to Amber
        "cooking" -> "Sedang Dimasak" to AccentOrange
        "ready" -> "Siap Diambil" to SuccessGreen
        "delivered" -> "Selesai" to SuccessGreen
        "cancelled" -> "Dibatalkan" to ErrorRed
        else -> status to Amber
    }
    Surface(color = container, shape = RoundedCornerShape(20.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EditOrderDialog(
    initialAddress: String,
    initialNotes: String,
    initialName: String,
    initialPhone: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    var address by remember { mutableStateOf(initialAddress) }
    var notes by remember { mutableStateOf(initialNotes) }
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Pesanan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor WhatsApp") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat pengiriman") },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber)
                )
                Text(
                    "Hanya bisa diubah selama pesanan masih berstatus Diterima.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, phone, address, notes) },
                enabled = !isUpdating
            ) {
                Text("Simpan", color = BurntAmber, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TrackingStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
