package com.dadarberedar.app.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
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
    var day by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }
    var paymentExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val paymentOptions = listOf(
        "Transfer Bank",
        "QRIS",
        "GoPay",
        "Bayar di Tempat (COD)"
    )

    val checkoutState by cartViewModel.checkoutState.collectAsState()

    LaunchedEffect(Unit) {
        val (savedName, savedPhone, _) = cartViewModel.getSavedUserDetails()
        if (name.isBlank() && !savedName.isNullOrBlank()) name = savedName
        if (phone.isBlank() && !savedPhone.isNullOrBlank()) phone = savedPhone
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is Resource.Success) {
            val order = (checkoutState as Resource.Success<Order>).data
            cartViewModel.resetCheckoutState()
            onOrderSuccess(order.orderNumber)
        }
    }

    val deliveryFee = 5000.0
    val finalTotal = cartViewModel.total + deliveryFee
    val isFormValid = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank()

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
                    Button(
                        onClick = {
                            val outletId = cartViewModel.selectedOutletId.value ?: 1L
                            cartViewModel.checkoutViaWhatsApp(
                                context = context,
                                outletId = outletId,
                                deliveryType = "express_delivery",
                                customerName = name.trim().ifBlank { "Sugeng Riyadhi" },
                                customerPhone = phone.trim().ifBlank { "08xxxxxxxxx" },
                                day = day.trim(),
                                date = date.trim(),
                                time = time.trim(),
                                deliveryAddress = address.trim(),
                                deliveryNotes = null,
                                paymentMethod = paymentMethod
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = isFormValid && checkoutState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Amber),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        if (checkoutState is Resource.Loading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Pesan Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
            // Nama
            Text("Nama", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Sugeng Riyadhi", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedContainerColor = SurfaceWhite
                ),
                singleLine = true
            )

            // Hari & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hari", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it },
                        placeholder = { Text("Senin", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedContainerColor = SurfaceWhite
                        ),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tanggal", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        placeholder = { Text("17-Agustus-1945", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedContainerColor = SurfaceWhite
                        ),
                        singleLine = true
                    )
                }
            }

            // No. Whatsapp & Jam
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("No. Whatsapp", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text("08xxxxxxxxx", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedContainerColor = SurfaceWhite
                        ),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Jam", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        placeholder = { Text("11.00", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DividerColor,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedContainerColor = SurfaceWhite
                        ),
                        singleLine = true
                    )
                }
            }

            // Alamat Pengiriman
            Text("Alamat Pengiriman", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("Jl. Mawar No. 10, Sidoarjo", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DividerColor,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedContainerColor = SurfaceWhite
                ),
                minLines = 2
            )

            // Metode Pembayaran
            Text("Metode Pembayaran", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paymentExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = true,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Amber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(paymentMethod, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 15.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
                DropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false }
                ) {
                    paymentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                paymentMethod = option
                                paymentExpanded = false
                            }
                        )
                    }
                }
            }

            // Ringkasan Pesanan
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ringkasan Pesanan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val itemCount = cartViewModel.cartItems.sumOf { it.quantity }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$itemCount Item", color = TextSecondary, fontSize = 14.sp)
                        Text("Rp ${String.format("%,.0f", cartViewModel.total)}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ongkos Kirim", color = TextSecondary, fontSize = 14.sp)
                        Text("Rp ${String.format("%,.0f", deliveryFee)}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Pembayaran", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Rp ${String.format("%,.0f", finalTotal)}",
                            color = AccentOrange,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
