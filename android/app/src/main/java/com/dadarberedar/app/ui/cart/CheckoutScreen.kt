package com.dadarberedar.app.ui.cart

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
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
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
            // Delivery Type
            Text("Jenis Pesanan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("takeaway" to "Takeaway", "dine_in" to "Dine In", "express_delivery" to "Express").forEach { (value, label) ->
                    FilterChip(
                        selected = deliveryType == value,
                        onClick = { deliveryType = value },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBrown,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            if (deliveryType == "express_delivery") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Biaya pengiriman express: Rp 12.000", fontSize = 12.sp, color = AccentOrange)
                    }
                }
            }

            // Customer Info
            Text("Informasi Pemesan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(10.dp)
            )

            if (deliveryType != "dine_in") {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Pengiriman *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(10.dp)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                shape = RoundedCornerShape(10.dp)
            )

            // Payment
            Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    listOf(
                        "qris" to "QRIS",
                        "midtrans_gopay" to "GoPay",
                        "midtrans_bca" to "Transfer BCA",
                        "cod" to "Bayar di Tempat (COD)"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == value,
                                onClick = { paymentMethod = value },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBrown)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Summary
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CartPriceRow("Subtotal", cartViewModel.subtotal)
                    Spacer(modifier = Modifier.height(4.dp))
                    CartPriceRow("Pajak (10%)", cartViewModel.tax)
                    if (deliveryType == "express_delivery") {
                        Spacer(modifier = Modifier.height(4.dp))
                        CartPriceRow("Biaya Pengiriman", 12000.0)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                    CartPriceRow("Total", cartViewModel.total, isBold = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = name.isNotBlank() && phone.isNotBlank() &&
                    (deliveryType == "dine_in" || address.isNotBlank()) &&
                    checkoutState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (checkoutState is Resource.Loading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Pesanan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
