package com.dadarberedar.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dadarberedar.app.ui.cart.CartScreen
import com.dadarberedar.app.ui.cart.CheckoutScreen
import com.dadarberedar.app.ui.components.BottomNavBar
import com.dadarberedar.app.ui.home.HomeScreen
import com.dadarberedar.app.ui.menu.MenuScreen
import com.dadarberedar.app.ui.outlet.OutletDetailScreen
import com.dadarberedar.app.ui.product.ProductDetailScreen
import com.dadarberedar.app.ui.profile.ProfileScreen
import com.dadarberedar.app.ui.search.SearchScreen
import com.dadarberedar.app.ui.splash.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadarberedar.app.ui.cart.CartViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Menu : Screen("menu")
    object Search : Screen("search")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Long) = "product/$productId"
    }
    object OutletDetail : Screen("outlet/{outletId}") {
        fun createRoute(outletId: Long) = "outlet/$outletId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Profile : Screen("profile")
}

private val bottomNavRoutes = listOf(
    Screen.Home.route,
    Screen.Menu.route,
    Screen.Cart.route,
    Screen.Profile.route
)

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val cartViewModel: CartViewModel = hiltViewModel()
    val cartCount = cartViewModel.cartItems.sumOf { it.quantity }

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    cartCount = cartCount
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onOutletClick = { outletId ->
                        navController.navigate(Screen.OutletDetail.createRoute(outletId))
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onCategoryClick = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onMenuClick = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Menu.route) {
                MenuScreen(
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOutletClick = { outletId ->
                        navController.navigate(Screen.OutletDetail.createRoute(outletId))
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(
                route = Screen.OutletDetail.route,
                arguments = listOf(navArgument("outletId") { type = NavType.LongType })
            ) {
                OutletDetailScreen(
                    onBack = { navController.popBackStack() },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onProceedToCheckout = { navController.navigate(Screen.Checkout.route) },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderSuccess = { _ ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
