package com.example.geosplit.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.geosplit.ui.screens.AboutScreen
import com.example.geosplit.ui.screens.CalculatorScreen
import com.example.geosplit.ui.screens.HistoryScreen
import com.example.geosplit.ui.screens.SettingsScreen
import com.example.geosplit.ui.screens.SplashScreen
import com.example.geosplit.viewmodel.GeoSplitViewModel

@Composable
fun GeoSplitApp(
    viewModel: GeoSplitViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSplashScreen = currentRoute == Screen.Splash.route

    Scaffold(
        bottomBar = {
            if (!isSplashScreen) {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Calculator.route) {
                CalculatorScreen(viewModel = viewModel)
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onOpenRecord = {
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(Screen.Calculator.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
