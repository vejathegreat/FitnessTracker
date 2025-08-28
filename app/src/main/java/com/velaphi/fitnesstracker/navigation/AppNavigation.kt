package com.velaphi.fitnesstracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.velaphi.authetication.presentation.navigation.AuthNavigation
import com.velaphi.authetication.presentation.viewmodel.AuthViewModel
import com.velaphi.fitnesstracker.MainScreen
import com.velaphi.fitnesstracker.SplashScreen

@Composable
fun AppNavigation(
    startDestination: String = "splash",
    navController: NavHostController = rememberNavController(),
    initialScreen: String? = null
) {
    // Debug logging
    LaunchedEffect(initialScreen) {
        println("AppNavigation: initialScreen received: $initialScreen")
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Check authentication state
        composable("splash") {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        // Authentication flow
        composable("auth") {
            val authViewModel: AuthViewModel = hiltViewModel()
            
            AuthNavigation(
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onSignOut = {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
        
        // Main app flow with bottom navigation
        composable("main") {
            val authViewModel: AuthViewModel = hiltViewModel()
            
            MainScreen(
                navController = navController,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                initialScreen = initialScreen
            )
        }
        

    }
}

