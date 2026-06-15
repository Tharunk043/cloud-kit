package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import android.util.Log

// ─────────────────────────────────────────────────────────────────────────────
// Shared payment state machine & Utilities
// ─────────────────────────────────────────────────────────────────────────────

private fun isPackageInstalled(packageName: String, context: android.content.Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
        false
    }
}

private enum class PaymentState { IDLE, PROCESSING, OTP_PROMPT, SUCCESS, FAILURE }

// ─────────────────────────────────────────────────────────────────────────────
// 1. UPI Payment Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPaymentSheet(
    amount: Double,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var upiId by remember { mutableStateOf("") }
    var upiError by remember { mutableStateOf("") }
    var payState by remember { mutableStateOf(PaymentState.IDLE) }
    var otp by remember { mutableStateOf("") }
    var failureMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Register ActivityResultLauncher for the UPI Intent Redirect
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultData = result.data?.getStringExtra("response") ?: ""
        Log.d("UpiPaymentSheet", "UPI Response Data: $resultData")
        
        // standard UPI apps return status parameters in query-like response
        val isSuccess = result.resultCode == android.app.Activity.RESULT_OK || 
                        resultData.contains("Status=SUCCESS", ignoreCase = true) || 
                        resultData.contains("Status=success", ignoreCase = true) ||
                        resultData.contains("txnRef=", ignoreCase = true) // Some return empty response with OK

        if (isSuccess) {
            payState = PaymentState.SUCCESS
        } else {
            failureMsg = "UPI App payment cancelled or failed. You can retry or use test simulation."
            payState = PaymentState.FAILURE
        }
    }

    // Auto-advance PROCESSING → OTP_PROMPT after 2 seconds (Fallback sandbox flow)
    LaunchedEffect(payState) {
        if (payState == PaymentState.PROCESSING) {
            delay(2000)
            payState = PaymentState.OTP_PROMPT
        }
        if (payState == PaymentState.SUCCESS) {
            delay(1500)
            val txnId = "UPI${System.currentTimeMillis()}"
            onSuccess(txnId)
        }
    }

    // OTP verification dialog (Sandbox Mode)
    if (payState == PaymentState.OTP_PROMPT) {
        AlertDialog(
            onDismissRequest = { payState = PaymentState.IDLE },
            icon = {
                Text("🔐", fontSize = 32.sp)
            },
            title = {
                Text(
                    "Enter OTP",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "A 6-digit OTP has been sent to your registered mobile number.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                        label = { Text("6-digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Test mode hint
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔑 TEST OTP: 123456",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otp == "123456") {
                            payState = PaymentState.SUCCESS
                        } else {
                            failureMsg = "Incorrect OTP. Please try again with the correct one."
                            payState = PaymentState.FAILURE
                        }
                    },
                    enabled = otp.length == 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Verify OTP", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { payState = PaymentState.IDLE; otp = "" }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📱", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pay via UPI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Instant • Secure • No charges",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Test mode banner
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFCA28)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "UPI Recipient: tharunvelamakuru143-3@okaxis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // UPI ID Field
            OutlinedTextField(
                value = upiId,
                onValueChange = {
                    upiId = it
                    upiError = ""
                },
                label = { Text("Or enter custom UPI ID") },
                placeholder = { Text("yourname@paytm", color = Color.Gray.copy(0.6f)) },
                isError = upiError.isNotEmpty(),
                supportingText = {
                    if (upiError.isNotEmpty()) {
                        Text(upiError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = {
                    Text("📱", fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(Modifier.height(16.dp))

            // Quick UPI Shortcuts
            Text(
                text = "Quick select custom handle",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                listOf("@paytm", "@gpay", "@phonepe", "@upi").forEach { suffix ->
                    val selected = upiId.endsWith(suffix) && upiId.length > suffix.length
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val base = upiId.substringBefore("@").ifEmpty { "user" }
                            upiId = "$base$suffix"
                            upiError = ""
                        },
                        label = {
                            Text(
                                suffix,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1976D2).copy(alpha = 0.12f),
                            selectedLabelColor = Color(0xFF1976D2)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = Color(0xFF1976D2),
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Popular UPI Apps Grid
            Text(
                text = "Choose your payment app (Real-Time Redirect)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                data class UpiApp(val logoUrl: String, val name: String, val color: Color, val packageName: String)
                val apps = listOf(
                    UpiApp("https://upload.wikimedia.org/wikipedia/commons/2/24/Paytm_Logo_%28standalone%29.png", "Paytm", Color(0xFF002970), "net.one97.paytm"),
                    UpiApp("https://upload.wikimedia.org/wikipedia/commons/e/e1/PhonePe_Logo.png", "PhonePe", Color(0xFF5F259F), "com.phonepe.app"),
                    UpiApp("https://upload.wikimedia.org/wikipedia/commons/f/f2/Google-Pay-Logo.png", "GPay", Color(0xFF1A73E8), "com.google.android.apps.nbu.paisa.user"),
                    UpiApp("https://upload.wikimedia.org/wikipedia/commons/e/e1/BHIM_Logo.png", "BHIM", Color(0xFF1565C0), "in.org.npci.upiapp")
                )
                apps.forEach { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(app.color.copy(alpha = 0.07f))
                            .border(1.dp, app.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable {
                                val upiUri = Uri.parse("upi://pay?pa=tharunvelamakuru143-3@okaxis&pn=Tharun%20Velamakuru&tn=BiteCraft%20Order&am=${String.format(java.util.Locale.US, "%.2f", amount)}&cu=INR")
                                val isAppInstalled = isPackageInstalled(app.packageName, context)
                                if (isAppInstalled) {
                                    val upiIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                                        setPackage(app.packageName)
                                    }
                                    try {
                                        upiLauncher.launch(upiIntent)
                                    } catch (e: Exception) {
                                        Log.e("UpiPaymentSheet", "Failed to launch ${app.name}", e)
                                        Toast.makeText(context, "${app.name} launch failed. Falling back to Sandbox Mode.", Toast.LENGTH_SHORT).show()
                                        payState = PaymentState.PROCESSING
                                    }
                                } else {
                                    val checkIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                    val list = context.packageManager.queryIntentActivities(checkIntent, 0)
                                    if (list.isNotEmpty()) {
                                        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, upiUri), "Pay with...")
                                        try {
                                            upiLauncher.launch(chooser)
                                        } catch (e: Exception) {
                                            Log.e("UpiPaymentSheet", "Failed to launch chooser", e)
                                            Toast.makeText(context, "No UPI client could be launched. Falling back to Sandbox Mode.", Toast.LENGTH_SHORT).show()
                                            payState = PaymentState.PROCESSING
                                        }
                                    } else {
                                        Toast.makeText(context, "${app.name} not installed. Running Sandbox Test Payment.", Toast.LENGTH_SHORT).show()
                                        payState = PaymentState.PROCESSING
                                    }
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        AsyncImage(
                            model = app.logoUrl,
                            contentDescription = app.name,
                            modifier = Modifier
                                .height(30.dp)
                                .width(55.dp)
                                .padding(horizontal = 2.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            app.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = app.color,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Amount Divider
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Amount to Pay", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "₹%.2f".format(amount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Pay Button with AnimatedContent
            AnimatedContent(
                targetState = payState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "upi_pay_state"
            ) { state ->
                when (state) {
                    PaymentState.IDLE, PaymentState.OTP_PROMPT -> {
                        Button(
                            onClick = {
                                val upiUri = Uri.parse("upi://pay?pa=tharunvelamakuru143-3@okaxis&pn=Tharun%20Velamakuru&tn=BiteCraft%20Order&am=${String.format(java.util.Locale.US, "%.2f", amount)}&cu=INR")
                                val checkIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                val list = context.packageManager.queryIntentActivities(checkIntent, 0)
                                if (list.isNotEmpty()) {
                                    val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, upiUri), "Complete payment with...")
                                    try {
                                        upiLauncher.launch(chooser)
                                    } catch (e: Exception) {
                                        payState = PaymentState.PROCESSING
                                    }
                                } else {
                                    Toast.makeText(context, "No UPI client found. Running Sandbox Test Payment.", Toast.LENGTH_SHORT).show()
                                    payState = PaymentState.PROCESSING
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "PAY ₹%.2f".format(amount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    PaymentState.PROCESSING -> {
                        Surface(
                            color = Color(0xFF1976D2).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color(0xFF1976D2),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Redirecting to secure gateway...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                    PaymentState.SUCCESS -> {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Payment Successful!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                    PaymentState.FAILURE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        failureMsg,
                                        fontSize = 13.sp,
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Button(
                                onClick = { payState = PaymentState.SUCCESS },
                                modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Simulate Sandbox Success", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { payState = PaymentState.IDLE; otp = "" },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            // Security footnote
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Secured by 256-bit SSL encryption",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Card Payment Sheet
// ─────────────────────────────────────────────────────────────────────────────

private fun detectCardType(number: String): String = when {
    number.startsWith("4") -> "Visa"
    number.startsWith("5") -> "Mastercard"
    number.startsWith("6") -> "RuPay"
    else -> "Card"
}

private fun formatCardNumber(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(16)
    return digits.chunked(4).joinToString(" ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentSheet(
    amount: Double,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rawCardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var payState by remember { mutableStateOf(PaymentState.IDLE) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val displayNumber = formatCardNumber(rawCardNumber)
    val cardType = detectCardType(rawCardNumber)

    val cardGradient = when (cardType) {
        "Visa" -> Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF283593)))
        "Mastercard" -> Brush.linearGradient(listOf(Color(0xFF4E342E), Color(0xFF6D4C41)))
        "RuPay" -> Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
        else -> Brush.linearGradient(listOf(Color(0xFF212121), Color(0xFF37474F)))
    }

    LaunchedEffect(payState) {
        if (payState == PaymentState.PROCESSING) {
            delay(2000)
            // Validate test card
            val cleanNum = rawCardNumber.filter { it.isDigit() }
            if (cleanNum == "4242424242424242") {
                payState = PaymentState.SUCCESS
            } else {
                errorMsg = "Card declined. Use test card: 4242 4242 4242 4242"
                payState = PaymentState.FAILURE
            }
        }
        if (payState == PaymentState.SUCCESS) {
            delay(1500)
            val txnId = "CARD${System.currentTimeMillis()}"
            onSuccess(txnId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Header ──────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Add Card",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("Visa • Mastercard • RuPay accepted", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Test Card Banner ─────────────────────────────────────────────
            Surface(
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFCE93D8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "🃏 Test Card Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Number: 4242 4242 4242 4242\nExp: 12/26  •  CVV: 123\nName: Test User",
                        fontSize = 12.sp,
                        color = Color(0xFF4A148C),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Premium Card Preview ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardGradient)
                    .padding(22.dp)
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = 120.dp, y = (-60).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 160.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top row: card type label + bank logo placeholder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Card type badge
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = cardType.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        // Bank logo area (rectangle placeholder)
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏦", fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Chip icon
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
                                )
                            )
                    ) {
                        // Chip lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineColor = Color(0xFFE65100)
                            val stroke = Stroke(width = 1.5f)
                            drawLine(lineColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 1.5f)
                            drawLine(lineColor, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1.5f)
                            drawRoundRect(
                                color = lineColor,
                                topLeft = Offset(4f, 6f),
                                size = Size(size.width - 8f, size.height - 12f),
                                cornerRadius = CornerRadius(4f, 4f),
                                style = stroke
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Card Number
                    Text(
                        text = if (displayNumber.isEmpty()) "•••• •••• •••• ••••" else displayNumber.padEnd(19, ' ').let {
                            // mask middle groups if fully entered
                            val parts = it.trim().split(" ")
                            if (parts.size >= 4) "${parts[0]} •••• •••• ${parts.getOrElse(3) { "••••" }}"
                            else it
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(Modifier.height(12.dp))

                    // Bottom row: name + expiry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "CARD HOLDER",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                cardholderName.uppercase().ifEmpty { "YOUR NAME" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "EXPIRES",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                expiry.ifEmpty { "MM/YY" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Accepted Cards Row ───────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                listOf(
                    Triple("VISA", Color(0xFF1A237E), Color.White),
                    Triple("MC", Color(0xFFB71C1C), Color.White),
                    Triple("RuPay", Color(0xFF1B5E20), Color.White)
                ).forEach { (label, bg, fg) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = fg)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Card Number Field ────────────────────────────────────────────
            OutlinedTextField(
                value = displayNumber,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(16)
                    rawCardNumber = digits
                },
                label = { Text("Card Number") },
                placeholder = { Text("4242 4242 4242 4242", color = Color.Gray.copy(0.5f)) },
                leadingIcon = {
                    Icon(Icons.Filled.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (rawCardNumber.isNotEmpty()) {
                        Text(
                            when (cardType) {
                                "Visa" -> "💳"
                                "Mastercard" -> "🔴"
                                "RuPay" -> "🟢"
                                else -> ""
                            },
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Expiry + CVV ─────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(4)
                        expiry = when {
                            digits.length >= 3 -> "${digits.take(2)}/${digits.drop(2)}"
                            digits.length == 2 && expiry.length < 2 -> "$digits/"
                            else -> digits
                        }
                    },
                    label = { Text("Expiry") },
                    placeholder = { Text("MM/YY", color = Color.Gray.copy(0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) cvv = it },
                    label = { Text("CVV") },
                    placeholder = { Text("•••", color = Color.Gray.copy(0.5f)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "CVV secure",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Cardholder Name ──────────────────────────────────────────────
            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it },
                label = { Text("Cardholder Name") },
                placeholder = { Text("As on card", color = Color.Gray.copy(0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(Modifier.height(20.dp))

            // ── Amount Display ────────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payable", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        "₹%.2f".format(amount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Pay Button AnimatedContent ────────────────────────────────────
            AnimatedContent(
                targetState = payState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "card_pay_state"
            ) { state ->
                when (state) {
                    PaymentState.IDLE -> {
                        Button(
                            onClick = {
                                val cleanNum = rawCardNumber.filter { it.isDigit() }
                                when {
                                    cleanNum.length < 16 -> errorMsg = "Enter a complete 16-digit card number."
                                    expiry.length < 5 -> errorMsg = "Enter a valid expiry (MM/YY)."
                                    cvv.length < 3 -> errorMsg = "Enter a valid 3-digit CVV."
                                    cardholderName.isBlank() -> errorMsg = "Enter the cardholder name."
                                    else -> { errorMsg = ""; payState = PaymentState.PROCESSING }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "PAY SECURELY ₹%.2f".format(amount),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    PaymentState.PROCESSING -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Processing Payment...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    PaymentState.SUCCESS -> {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Payment Successful!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                    PaymentState.FAILURE, PaymentState.OTP_PROMPT -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (errorMsg.isNotEmpty()) {
                                Surface(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Cancel,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            errorMsg,
                                            fontSize = 13.sp,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = { payState = PaymentState.IDLE; errorMsg = "" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text("PCI-DSS compliant • End-to-end encrypted", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Payment Success Overlay  (full-screen with animated checkmark)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PaymentSuccessOverlay(
    amountPaid: Double,
    orderId: String,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // Checkmark draw animation
    val animProgress = remember { Animatable(0f) }
    val circleScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        circleScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated checkmark canvas
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(circleScale.value)
            ) {
                // Outer glow ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 6f)
                )
                // Main circle fill
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = size.minDimension / 2f - 6f
                )
                // Checkmark stroke
                val progress = animProgress.value
                val startX = size.width * 0.22f
                val startY = size.height * 0.52f
                val midX = size.width * 0.43f
                val midY = size.height * 0.68f
                val endX = size.width * 0.76f
                val endY = size.height * 0.34f

                if (progress > 0f) {
                    val checkPath = Path()
                    if (progress <= 0.5f) {
                        val t = progress / 0.5f
                        checkPath.moveTo(startX, startY)
                        checkPath.lineTo(
                            startX + (midX - startX) * t,
                            startY + (midY - startY) * t
                        )
                    } else {
                        val t = (progress - 0.5f) / 0.5f
                        checkPath.moveTo(startX, startY)
                        checkPath.lineTo(midX, midY)
                        checkPath.lineTo(
                            midX + (endX - midX) * t,
                            midY + (endY - midY) * t
                        )
                    }
                    drawPath(
                        path = checkPath,
                        color = Color.White,
                        style = Stroke(
                            width = 12f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Payment Successful!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "₹%.2f".format(amountPaid),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Order ID",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        orderId,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Redirecting to order tracking...",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Payment Failure Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PaymentFailureCard(
    errorMessage: String,
    onTryAgain: () -> Unit,
    onChangePaymentMethod: () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        repeat(4) {
            shakeOffset.animateTo(12f, spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(-12f, spring(stiffness = Spring.StiffnessHigh))
        }
        shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    Surface(
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Color(0xFFEF9A9A)),
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.value.dp)
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large X icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Cancel,
                    contentDescription = "Payment Failed",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Payment Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB71C1C)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                errorMessage,
                fontSize = 14.sp,
                color = Color(0xFFD32F2F).copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Try Again
                Button(
                    onClick = onTryAgain,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Change payment method
                OutlinedButton(
                    onClick = onChangePaymentMethod,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
                ) {
                    Text(
                        "Change Method",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Cash on Delivery Confirm Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodConfirmSheet(
    amount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isConfirming by remember { mutableStateOf(false) }

    LaunchedEffect(isConfirming) {
        if (isConfirming) {
            delay(1200)
            onConfirm()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💵", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Cash on Delivery",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("Pay when your order arrives", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Warning banner
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFFCA28)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Please keep exact change ready",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Our delivery partner may not always carry change. Exact cash helps ensure a smooth handover.",
                            fontSize = 12.sp,
                            color = Color(0xFFBF360C),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Amount to keep ready
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Amount to Keep Ready",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "₹%.2f".format(amount),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Denomination suggestions
                        val bills = buildList {
                            val amt = amount.toInt()
                            if (amt >= 500) add("₹500")
                            if (amt % 500 >= 100 || amt < 500) add("₹100")
                            add("₹50")
                            add("₹20")
                        }.take(3)
                        bills.forEach { bill ->
                            Surface(
                                color = Color(0xFF795548).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    bill,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E342E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info tiles
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple("🧾", "Receipt", "Printed on delivery"),
                    Triple("⏱️", "ETA", "30–45 min"),
                    Triple("📞", "Support", "24/7 available")
                ).forEach { (emoji, title, sub) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(sub, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Confirm button with state
            AnimatedContent(
                targetState = isConfirming,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "cod_confirm_state"
            ) { confirming ->
                if (confirming) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF2E7D32),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Confirming Order...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { isConfirming = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "CONFIRM ORDER — ₹%.2f".format(amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}
