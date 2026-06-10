package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlatformViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PlatformViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()
            
            var showSplash by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2200)
                showSplash = false
            }
            
            if (showSplash) {
                SplashScreenView()
            } else {
                MyApplicationTheme(darkTheme = isDark) {
                    // Track customer screen internally for simple navigation stability
                    var currentCustomerScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    // Automatically navigate to Detail screen if restaurant selection changes
                    LaunchedEffect(viewModel.selectedRestaurantId.collectAsState().value) {
                        if (viewModel.selectedRestaurantId.value != null) {
                            currentCustomerScreen = Screen.Detail
                        }
                    }

                    // Pure client application with Edge-to-Edge experience
                    Box(modifier = Modifier.fillMaxSize()) {
                        CustomerSection(
                            viewModel = viewModel,
                            currentScreen = currentCustomerScreen,
                            onNavigate = { screen ->
                                currentCustomerScreen = screen
                            }
                        )
                    }
                }
            }
        }
    }
}
