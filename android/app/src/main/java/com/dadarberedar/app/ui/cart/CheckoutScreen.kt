package com.dadarberedar.app.ui.cart

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
import com.dadarberedar.app.data.model.Order
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderSuccess: (String) -> Unit,
    cartViewModel: CartViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var deliveryType by remember { mutableStateOf("takeaway") }
    var paymentMethod by remember { mutableStateOf("qris") }
    var paymentExpanded by remember { mutableStateOf(false) }

    val paymentOptions = listOf(
        "qris" to "QRIS",
        "midtrans_gopay" to "GoPay",
        "midtrans_bca" to "Transfer BCA",
        "cod" to "Bayar di Tempat (COD)"
    )

    val checkoutState by cartViewModel.checkoutState.collectAsState()

    LaunchedEffect(checkoutState) {
        if (checkoutState is Resource.Success) {
            val order = (checkoutState as Resource.Success<Order>).data
            cartViewModel.resetCheckoutState()
            onOrderSuccess(order.orderNumber)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
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
                                "Rp ${String.format("%,.0f", cartViewModel.total + if (deliveryType == "express_delivery") 12000.0 else 0.0)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                        }
                        Button(
                            onClick = {
                                val outletId = cartViewModel.selectedOutletId.value ?: 1L
                                cartViewModel.checkout(
                                    token = null,
                                    outletId = outletId,
                                    deliveryType = deliveryType,
                                    customerName = name.ifBlank { "Tamu" },
                                    customerPhone = phone.ifBlank { "0000" },
                                    customerEmail = email.ifBlank { null },
                                    deliveryAddress = if (deliveryType != "dine_in") address.ifBlank { null } else null,
                                    deliveryNotes = notes.ifBlank { null },
                                    paymentMethod = paymentMethod
                                )
                            },
                            modifier = Modifier.height(48.dp),
                            enabled = name.isNotBlank() && phone.isNotBlank() &&
                                (deliveryType == "dine_in" || address.isNotBlank()) &&
                                checkoutState !is Resource.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Amber),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            if (checkoutState is Resource.Loading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Pesan Sekarang", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckoutSectionTitle("Jenis Pesanan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "takeaway" to "Takeaway",
                    "dine_in" to "Dine In",
                    "express_delivery" to "Express"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = deliveryType == value,
                        onClick = { deliveryType = value },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            if (deliveryType == "express_delivery") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BurntAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Biaya pengiriman express: Rp 12.000", fontSize = 12.sp, color = BurntAmber)
                    }
                }
            }

            // Alamat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alamat", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                TextButton(onClick = { }) {
                    Text("Ubah", fontSize = 13.sp, color = BurntAmber, fontWeight = FontWeight.SemiBold)
                }
            }
            if (deliveryType == "dine_in") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = BurntAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Makan di outlet Dadar Beredar", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("Jl. Contoh No. 123, Sidoarjo") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        unfocusedBorderColor = DividerColor
                    )
                )
            }

            // Informasi pemesan
            CheckoutSectionTitle("Informasi Pemesan")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor
                )
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor WhatsApp *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor
                )
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor
                )
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor
                )
            )

            // Metode pembayaran (dropdown sesuai Figma)
            CheckoutSectionTitle("Metode Pembayaran")
            ExposedDropdownMenuBox(
                expanded = paymentExpanded,
                onExpandedChange = { paymentExpanded = it }
            ) {
                OutlinedTextField(
                    value = paymentOptions.find { it.first == paymentMethod }?.second ?: "Pilih metode pembayaran",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        unfocusedBorderColor = DividerColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false }
                ) {
                    paymentOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                paymentMethod = value
                                paymentExpanded = false
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = paymentMethod == value,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Amber)
                                )
                            }
                        )
                    }
                }
            }

            // Ringkasan pesanan
            CheckoutSectionTitle("Ringkasan Pesanan")
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CartPriceRow("Subtotal", cartViewModel.subtotal)
                    Spacer(modifier = Modifier.height(6.dp))
                    CartPriceRow("Pajak (10%)", cartViewModel.tax)
                    if (deliveryType == "express_delivery") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CartPriceRow("Biaya Pengiriman", 12000.0)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)
                    CartPriceRow("Total Pembayaran", cartViewModel.total, isBold = true)
                }
            }

            if (checkoutState is Resource.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = (checkoutState as Resource.Error).message,
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

@Composable
private fun CheckoutSectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
}
