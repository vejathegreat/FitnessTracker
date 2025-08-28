package com.velaphi.authetication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.velaphi.authetication.presentation.screens.ForgotPasswordScreen
import com.velaphi.authetication.presentation.screens.LoginScreen
import com.velaphi.authetication.presentation.screens.ProfileScreen
import com.velaphi.authetication.presentation.screens.SignUpScreen
import com.velaphi.authetication.presentation.viewmodel.AuthViewModel

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
    object Profile : AuthScreen("profile")
    object ForgotPassword : AuthScreen("forgot_password")
}

@Composable
fun AuthNavigation(
    onNavigateToHome: () -> Unit,
    onSignOut: () -> Unit,
    navController: NavHostController = rememberNavController(),
    viewModel: AuthViewModel
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Automatically navigate to home when user is logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onNavigateToHome()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) AuthScreen.Profile.route else AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AuthScreen.ForgotPassword.route)
                },
                onNavigateToHome = onNavigateToHome
            )
        }
        
        composable(AuthScreen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigateUp()
                },
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(AuthScreen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    onSignOut()
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.Profile.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
