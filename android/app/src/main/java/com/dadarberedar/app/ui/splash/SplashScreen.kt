package com.dadarberedar.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadarberedar.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBrownDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = AccentGold
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = PrimaryBrownDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "DADAR",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = White,
                letterSpacing = 6.sp
            )
            Text(
                "BEREDAR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = AccentGold,
                letterSpacing = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Telur Dadar Krispy & Sambal Rempah",
                fontSize = 12.sp,
                color = White.copy(alpha = 0.6f)
            )
        }
    }
}
