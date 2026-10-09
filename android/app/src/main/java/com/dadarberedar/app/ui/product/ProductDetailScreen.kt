package com.dadarberedar.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dadarberedar.app.data.model.Product
import com.dadarberedar.app.data.model.resolvedImageUrl
import com.dadarberedar.app.ui.cart.CartViewModel
import com.dadarberedar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    var quantity by remember { mutableIntStateOf(1) }
    var addedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Menu", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = AccentOrange) { Text("$cartCount", color = White) }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            if (product != null) {
                val data = product!!
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Harga", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "Rp ${String.format("%,.0f", data.price * quantity)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                        }
                        Button(
                            onClick = {
                                val outletId = cartViewModel.selectedOutletId.value ?: 1L
                                cartViewModel.addToCart(data, outletId, quantity)
                                addedMessage = "${data.name} x$quantity ditambahkan ke keranjang"
                            },
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber),
                            shape = RoundedCornerShape(25.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tambah ke Keranjang", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(BackgroundCream)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = Amber,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(error!!, color = ErrorRed, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.loadProduct() }) {
                            Text("Coba Lagi")
                        }
                    }
                }

                product != null -> {
                    val data = product!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = data.resolvedImageUrl,
                            contentDescription = data.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(GrayLight)
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (data.isSignature) {
                                    BadgeTag(text = "Signature", container = AccentOrange)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                if (data.isBestseller) {
                                    BadgeTag(text = "Bestseller", container = Amber)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                if (data.isCrispy) {
                                    BadgeTag(text = "Krispy", container = BurntAmber)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                data.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Rp ${String.format("%,.0f", data.price)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )

                            if (data.spicinessLevel > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Level pedas:", fontSize = 13.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    repeat(data.spicinessLevel) {
                                        Text("🌶", fontSize = 13.sp)
                                    }
                                }
                            }

                            if (!data.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Deskripsi",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    data.description ?: "",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 21.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Jumlah", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        if (data.isAvailable) "Stok tersedia" else "Stok habis",
                                        fontSize = 12.sp,
                                        color = if (data.isAvailable) SuccessGreen else ErrorRed
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        onClick = { if (quantity > 1) quantity-- },
                                        shape = CircleShape,
                                        color = GrayLight,
                                        enabled = quantity > 1
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Kurangi",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .padding(9.dp),
                                            tint = if (quantity > 1) TextPrimary else TextTertiary
                                        )
                                    }
                                    Text(
                                        "$quantity",
                                        modifier = Modifier.widthIn(min = 44.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        onClick = { quantity++ },
                                        shape = CircleShape,
                                        color = Amber
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Tambah",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .padding(9.dp),
                                            tint = White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            addedMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = TextPrimary,
                    contentColor = White
                ) {
                    Text(message, fontSize = 13.sp)
                }
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(2500)
                    addedMessage = null
                }
            }
        }
    }
}

@Composable
private fun BadgeTag(text: String, container: androidx.compose.ui.graphics.Color) {
    Surface(color = container, shape = RoundedCornerShape(5.dp)) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
