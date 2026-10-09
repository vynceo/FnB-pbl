package com.dadarberedar.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadarberedar.app.R
import com.dadarberedar.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-90).dp, y = (-90).dp)
                .clip(CircleShape)
                .background(Amber.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .clip(CircleShape)
                .background(Amber.copy(alpha = 0.14f))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = (-120).dp)
                .clip(CircleShape)
                .background(BurntAmber.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(46.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 120.dp)
                .clip(CircleShape)
                .background(BurntAmber.copy(alpha = 0.16f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = White,
                shadowElevation = 8.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_pan),
                    contentDescription = "Logo Dadar Beredar",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "Rumah Makan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Dadar Beredar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = CircleShape,
                color = Amber
            ) {
                Text(
                    "Rasa Lezat, Bikin Nagih Setiap Saat",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }

        Text(
            "v1.0.0",
            fontSize = 11.sp,
            color = TextTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
