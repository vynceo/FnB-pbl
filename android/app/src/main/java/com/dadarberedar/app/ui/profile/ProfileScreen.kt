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
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = White
                )
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
            // Profile Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBrown
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AccentGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isLoggedIn) userName else "Tamu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    if (isLoggedIn && userEmail.isNotBlank()) {
                        Text(
                            userEmail,
                            fontSize = 13.sp,
                            color = White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoggedIn) {
                // Guest prompt
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = PrimaryBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Masuk untuk menikmati semua fitur", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrown),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Masuk", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Daftar Akun Baru", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            } else {
                // Profile Menu
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.Receipt,
                            title = "Pesanan Saya",
                            subtitle = "Lihat riwayat pesanan",
                            onClick = { /* navigate to orders */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.LocationOn,
                            title = "Alamat Tersimpan",
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.Help,
                            title = "Bantuan",
                            subtitle = "FAQ dan dukungan",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "Tentang Kami",
                            subtitle = "Dadar Beredar - Franchise by Babe Cabita & King Abdi",
                            onClick = { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Logout
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { viewModel.logout() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Keluar", color = ErrorRed, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Info
            Text(
                "Dadar Beredar v1.0.0",
                fontSize = 12.sp,
                color = TextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                "Telur Dadar Krispy, Sambal Rempah Legendaris",
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryBrown,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
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
