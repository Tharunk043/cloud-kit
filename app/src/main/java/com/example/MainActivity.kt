package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.LoginScreen
import com.example.data.api.WhatsAppOtpClient
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlatformViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PlatformViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(2800)
                showSplash = false
            }

            MyApplicationTheme(darkTheme = isDark) {
                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(400))
                    },
                    label = "SplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        EnterpriseSplashScreen()
                    } else if (!isLoggedIn) {
                        // ── WhatsApp OTP Login Gate
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
                        // Track customer screen internally for simple navigation stability
                        var currentCustomerScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                        val showFamily by viewModel.showFamilyScreen.collectAsState()
                        val showSettings by viewModel.showSettingsScreen.collectAsState()
                        val isGold by viewModel.isGoldMember.collectAsState()

                        // Automatically navigate to Detail screen if restaurant selection changes
                        LaunchedEffect(viewModel.selectedRestaurantId.collectAsState().value) {
                            if (viewModel.selectedRestaurantId.value != null) {
                                currentCustomerScreen = Screen.Detail
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                showFamily -> {
                                    FamilyManagementScreen(
                                        onBack = { viewModel.showFamilyScreen.value = false },
                                        onAddMember = { name, email, limit, color ->
                                            viewModel.addFamilyMember(name, email, limit, color)
                                        },
                                        onRemoveMember = { id ->
                                            viewModel.removeFamilyMember(id)
                                        },
                                        onUpdateLimit = { id, limit ->
                                            viewModel.updateMemberSpendingLimit(id, limit)
                                        },
                                        familyMembersFlow = viewModel.familyMembers.collectAsState().value
                                    )
                                }
                                showSettings -> {
                                    SettingsScreen(
                                        onNavigateToFamily = {
                                            viewModel.showSettingsScreen.value = false
                                            viewModel.showFamilyScreen.value = true
                                        },
                                        isDarkTheme = isDark,
                                        onToggleTheme = { viewModel.toggleTheme() },
                                        isGoldMember = isGold,
                                        onToggleGold = { viewModel.toggleGoldMembership() },
                                        onBack = { viewModel.showSettingsScreen.value = false }
                                    )
                                }
                                else -> {
                                    CustomerSection(
                                        viewModel = viewModel,
                                        currentScreen = currentCustomerScreen,
                                        onNavigate = { screen ->
                                            currentCustomerScreen = screen
                                        },
                                        onOpenSettings = {
                                            viewModel.showSettingsScreen.value = true
                                        },
                                        onOpenFamily = {
                                            viewModel.showFamilyScreen.value = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnterpriseSplashScreen() {
    var iconScale by remember { mutableStateOf(0f) }
    var textAlpha by remember { mutableStateOf(0f) }
    var taglineAlpha by remember { mutableStateOf(0f) }
    var dotVisible by remember { mutableStateOf(false) }

    val iconScaleAnim by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )
    val textAlphaAnim by animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = tween(600, easing = LinearOutSlowInEasing),
        label = "textAlpha"
    )
    val taglineAlphaAnim by animateFloatAsState(
        targetValue = taglineAlpha,
        animationSpec = tween(600, easing = LinearOutSlowInEasing),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        delay(200)
        iconScale = 1f
        delay(400)
        textAlpha = 1f
        delay(300)
        taglineAlpha = 1f
        delay(200)
        dotVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative rings
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6B35).copy(alpha = 0.08f))
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED).copy(alpha = 0.1f))
                .align(Alignment.Center)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Icon with bounce animation
            Box(
                modifier = Modifier
                    .scale(iconScaleAnim)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFF7C3AED))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fastfood,
                    contentDescription = "BiteCraft",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand name
            Text(
                text = "BiteCraft",
                color = Color(0xFF12101E),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.graphicsLayer { alpha = textAlphaAnim }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enterprise badge
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF6B35).copy(alpha = 0.15f), Color(0xFF7C3AED).copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ENTERPRISE EDITION",
                    color = Color(0xFF7C3AED),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.graphicsLayer { alpha = taglineAlphaAnim }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "Fast Delivery • AI Powered • Family Plans",
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = taglineAlphaAnim }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            AnimatedVisibility(visible = dotVisible) {
                LinearProgressIndicator(
                    color = Color(0xFFFF6B35),
                    trackColor = Color.Black.copy(alpha = 0.1f),
                    modifier = Modifier.width(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v2.0.0 · Powered by Gemini AI",
                color = Color.Black.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.graphicsLayer { alpha = taglineAlphaAnim }
            )
        }
    }
}
