package com.dadarberedar.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadarberedar.app.data.remote.AuthManager
import com.dadarberedar.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoggedIn.value = authManager.isLoggedIn()
            if (_isLoggedIn.value) {
                _userName.value = authManager.getUserName() ?: "Pengguna"
                _userEmail.value = authManager.getUserEmail() ?: ""
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
            _isLoggedIn.value = false
            _userName.value = ""
            _userEmail.value = ""
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onOrdersClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profil", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(BackgroundCream)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar + nama
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Amber,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isLoggedIn && userName.isNotBlank()) userName.first().uppercase() else "T",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (isLoggedIn) userName else "Tamu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isLoggedIn && userEmail.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(userEmail, fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { clipboard.setText(AnnotatedString(userEmail)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Salin",
                                tint = BurntAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isLoggedIn) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = BurntAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Masuk untuk menikmati semua fitur", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Masuk", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Daftar Akun Baru", fontSize = 15.sp)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.ReceiptLong,
                            title = "Riwayat Pesanan",
                            subtitle = "Lihat status & pesanan sebelumnya",
                            onClick = onOrdersClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.LocationOn,
                            title = "Alamat Saya",
                            subtitle = "Kelola alamat pengiriman",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.CreditCard,
                            title = "Metode Pembayaran",
                            subtitle = "Kelola metode pembayaran",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.Help,
                            title = "Bantuan",
                            subtitle = "FAQ dan dukungan",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "Tentang Aplikasi",
                            subtitle = "Dadar Beredar v1.0.0",
                            onClick = { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Rasa Lezat, Bikin Nagih Setiap Saat",
                fontSize = 11.sp,
                color = TextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Amber.copy(alpha = 0.14f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = BurntAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
