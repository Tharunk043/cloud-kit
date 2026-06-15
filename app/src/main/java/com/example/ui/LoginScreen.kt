package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LoginStep { PHONE, OTP, PROFILE }

@Composable
fun LoginScreen(
    onLoginSuccess: (phone: String, name: String) -> Unit,
    onSendOtp: suspend (phone: String) -> Pair<Boolean, String?>, // returns (sent, debugOtp)
    onVerifyOtp: (phone: String, otp: String) -> Pair<Boolean, String> // returns (verified, message)
) {
    var step by remember { mutableStateOf(LoginStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var debugOtp by remember { mutableStateOf<String?>(null) }
    var resendTimer by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Resend countdown
    LaunchedEffect(step) {
        if (step == LoginStep.OTP) {
            resendTimer = 30
            while (resendTimer > 0) {
                delay(1000)
                resendTimer--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF8F0), Color(0xFFFFEDD8), Color.White)
                )
            )
    ) {
        // Top orange wave decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFC8019), Color(0xFFFF6D00), Color(0xFFFFEDD8))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // App logo + name
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("🍛", fontSize = 44.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "BiteCraft",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Andhra's Finest Delivered Fast",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(32.dp))

            // Login card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "login_step"
                    ) { currentStep ->
                        when (currentStep) {
                            LoginStep.PHONE -> PhoneStep(
                                phone = phone,
                                onPhoneChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                                isLoading = isLoading,
                                error = error,
                                onSend = {
                                    if (phone.length != 10) {
                                        error = "Enter a valid 10-digit mobile number"
                                        return@PhoneStep
                                    }
                                    error = ""
                                    scope.launch {
                                        isLoading = true
                                        val fullPhone = "91$phone"
                                        val (sent, dOtp) = onSendOtp(fullPhone)
                                        debugOtp = dOtp
                                        isLoading = false
                                        step = LoginStep.OTP
                                    }
                                }
                            )
                            LoginStep.OTP -> OtpStep(
                                phone = phone,
                                otp = otp,
                                onOtpChange = { otp = it.filter { c -> c.isDigit() }.take(6) },
                                isLoading = isLoading,
                                error = error,
                                debugOtp = debugOtp,
                                resendTimer = resendTimer,
                                onVerify = {
                                    if (otp.length != 6) {
                                        error = "Enter the 6-digit OTP"
                                        return@OtpStep
                                    }
                                    error = ""
                                    scope.launch {
                                        isLoading = true
                                        val fullPhone = "91$phone"
                                        val (verified, msg) = onVerifyOtp(fullPhone, otp)
                                        isLoading = false
                                        if (verified) {
                                            step = LoginStep.PROFILE
                                        } else {
                                            error = msg
                                        }
                                    }
                                },
                                onResend = {
                                    scope.launch {
                                        isLoading = true
                                        val fullPhone = "91$phone"
                                        val (_, dOtp) = onSendOtp(fullPhone)
                                        debugOtp = dOtp
                                        isLoading = false
                                        resendTimer = 30
                                        otp = ""
                                        error = ""
                                    }
                                },
                                onBack = { step = LoginStep.PHONE; error = ""; otp = "" }
                            )
                            LoginStep.PROFILE -> ProfileStep(
                                name = name,
                                onNameChange = { name = it },
                                isLoading = isLoading,
                                onContinue = {
                                    val displayName = name.trim().ifEmpty { "Foodie" }
                                    onLoginSuccess("91$phone", displayName)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "By continuing, you agree to our Terms & Privacy Policy",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhoneStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    error: String,
    onSend: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter your mobile number", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(4.dp))
        Text("We'll send an OTP via WhatsApp", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        // WhatsApp badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF25D366).copy(alpha = 0.1f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("💬", fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text("OTP will be sent via WhatsApp", fontSize = 12.sp, color = Color(0xFF128C7E), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))

        // Phone input
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Mobile Number") },
            placeholder = { Text("98765 43210") },
            prefix = { Text("+91  ", fontWeight = FontWeight.Bold, color = Color(0xFFFC8019)) },
            leadingIcon = { Icon(Icons.Filled.Phone, null, tint = Color(0xFFFC8019)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSend() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFC8019)),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty()
        )
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSend,
            enabled = !isLoading && phone.length == 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Sending OTP...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("Send OTP via WhatsApp", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun OtpStep(
    phone: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    error: String,
    debugOtp: String?,
    resendTimer: Int,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Filled.ArrowBack, null, tint = Color(0xFFFC8019))
        }
        Text("Verify OTP", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(4.dp))
        Text(
            "Sent to +91-${phone.take(5)}XXXXX via WhatsApp",
            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // Debug OTP banner (shown only when Evolution API not configured)
        if (debugOtp != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Test Mode — WhatsApp API not configured", fontSize = 11.sp, color = Color(0xFF5D4037))
                        Text("Your OTP: $debugOtp", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFE65100))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 6-digit OTP box input
        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("6-Digit OTP") },
            placeholder = { Text("_ _ _ _ _ _") },
            leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFFFC8019)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onVerify() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFC8019)),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty(),
            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp, textAlign = TextAlign.Center)
        )
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onVerify,
            enabled = !isLoading && otp.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Verifying...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Icon(Icons.Filled.Verified, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Verify & Continue", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Resend timer
        if (resendTimer > 0) {
            Text("Resend OTP in ${resendTimer}s", fontSize = 13.sp, color = Color.Gray)
        } else {
            TextButton(onClick = onResend) {
                Text("Resend OTP via WhatsApp", color = Color(0xFFFC8019), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    isLoading: Boolean,
    onContinue: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("✅", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text("Number Verified!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2E7D32))
        Spacer(Modifier.height(4.dp))
        Text("What should we call you?", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Tharun") },
            leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFFFC8019)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(onDone = { onContinue() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFC8019)),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
        ) {
            Text("Start Ordering 🍛", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}
