package com.velaphi.fitnesstracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.velaphi.fitnesstracker.navigation.AppNavigation
import com.velaphi.fitnesstracker.ui.theme.FitnessTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var initialScreen: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)
        
        setContent {
            FitnessTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(initialScreen = initialScreen)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("MainActivity: onNewIntent called with action: ${intent?.action}")
        handleIntent(intent)
        
        if (initialScreen != null) {
            setContent {
                FitnessTrackerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(initialScreen = initialScreen)
                    }
                }
            }
        }
    }
    
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                Intent.ACTION_MAIN -> {
                    // App launched normally
                    initialScreen = null
                }
                Intent.ACTION_VIEW -> {
                    // Handle deep link
                    it.data?.let { uri ->
                        initialScreen = when (uri.host) {
                            "tracker" -> "tracker"
                            "goals" -> "goals"
                            "meals" -> "meals"
                            "summary" -> "summary"
                            else -> null
                        }
                        
                        // Log for debugging
                        println("Deep link handled: ${uri.host} -> $initialScreen")
                    }
                }
                else -> {
                    // Handle notification or other intents
                    val openScreen = it.getStringExtra("open_screen")
                    val fromNotification = it.getBooleanExtra("from_notification", false)
                    
                    if (fromNotification && openScreen == "tracker") {
                        // Ensure we navigate to tracker when coming from notification
                        initialScreen = "tracker"
                    } else if (!openScreen.isNullOrEmpty()) {
                        initialScreen = openScreen
                    } else {
                        initialScreen = null
                    }
                    
                    // Log for debugging
                    println("Intent handled: action=${it.action}, openScreen=$openScreen, fromNotification=$fromNotification, initialScreen=$initialScreen")
                }
            }
        }
    }
}