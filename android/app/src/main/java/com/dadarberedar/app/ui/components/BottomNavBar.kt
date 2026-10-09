package com.dadarberedar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dadarberedar.app.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Menu : BottomNavItem("menu", "Menu", Icons.Filled.Restaurant, Icons.Outlined.Restaurant)
    object Cart : BottomNavItem("cart", "Keranjang", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Profile : BottomNavItem("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    cartCount: Int = 0
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Menu,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = SurfaceWhite,
        contentColor = Amber,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val iconColor by animateColorAsState(
                targetValue = if (selected) Amber else TextTertiary,
                label = "iconColor"
            )
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    if (item is BottomNavItem.Cart && cartCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = AccentGold) {
                                    Text("$cartCount", color = White)
                                }
                            }
                        ) {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        color = iconColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Amber.copy(alpha = 0.12f)
                )
            )
        }
    }
}
