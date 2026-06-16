package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RiderSection
import com.example.ui.LoginScreen
import com.example.data.api.WhatsAppOtpClient
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlatformViewModel

class RiderMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestPermissionsLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val postNotificationsGranted = permissions[android.Manifest.permission.POST_NOTIFICATIONS] ?: false
            android.util.Log.d("RiderMainActivity", "Permissions callback: fine=$fineLocationGranted, coarse=$coarseLocationGranted, notify=$postNotificationsGranted")
        }

        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())

        setContent {
            val viewModel: PlatformViewModel = viewModel()
            viewModel.switchRole("Rider")
            val isDark by viewModel.isDarkTheme.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        LoginScreen(
                            onLoginSuccess = { phone, name ->
                                viewModel.loginUser(phone, name)
                            },
                            onSendOtp = { phone ->
                                val result = WhatsAppOtpClient.sendOtp(phone)
                                Pair(result.otpSent, result.debugOtp)
                            },
                            onVerifyOtp = { phone, otp ->
                                val result = WhatsAppOtpClient.verifyOtp(phone, otp)
                                Pair(result.verified, result.message)
                            }
                        )
                    } else {
                        RiderSection(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
