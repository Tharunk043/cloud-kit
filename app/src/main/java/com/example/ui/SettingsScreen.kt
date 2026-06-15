package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────────────────
// Accent colours used throughout this screen
// ─────────────────────────────────────────────────────────────────────────────
private val OrangeGradientStart = Color(0xFFFC8019)
private val OrangeGradientEnd   = Color(0xFFE64A19)
private val GoldColour          = Color(0xFFFFD54F)
private val PurpleAccent        = Color(0xFF9C27B0)
private val GreenAccent         = Color(0xFF4CAF50)
private val RedLogout           = Color(0xFFE53935)

// ─────────────────────────────────────────────────────────────────────────────
// 1. SettingsScreen — Main entry-point composable
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToFamily: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isGoldMember: Boolean,
    onToggleGold: () -> Unit,
    userName: String,
    userPhone: String,
    userEmail: String,
    onSaveProfile: (String, String) -> Unit,
    onNavigateToOrders: () -> Unit,
    onBack: () -> Unit = {}
) {
    // Local UI state
    var showUpiDialog      by remember { mutableStateOf(false) }
    var orderUpdatesOn     by remember { mutableStateOf(true) }
    var promotionsOn       by remember { mutableStateOf(true) }
    var showProfileDialog  by remember { mutableStateOf(false) }
    var showLegalDialog    by remember { mutableStateOf<String?>(null) }

    // Simulated family member count (non-zero = active plan)
    val familyMemberCount  = remember { 3 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(OrangeGradientStart, OrangeGradientEnd)
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OrangeGradientStart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 100.dp
            )
        ) {

            // ── Hero gradient header ─────────────────────────────────────────
            item {
                SettingsHeader(isGoldMember = isGoldMember, onToggleGold = onToggleGold)
            }

            // ── ACCOUNT section ──────────────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "Account",
                    accentColor = OrangeGradientStart
                )
            }

            item {
                SettingsSectionCard {
                    // Profile row
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        iconBackground = Color(0xFFFFF3E0),
                        iconTint = OrangeGradientStart,
                        title = "Profile Settings",
                        subtitle = "${userName.ifEmpty { "Foodie" }}  •  $userPhone",
                        onClick = { showProfileDialog = true },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    SettingsRow(
                        icon = Icons.Outlined.Receipt,
                        iconBackground = Color(0xFFE0F7FA),
                        iconTint = Color(0xFF0097A7),
                        title = "Your Orders",
                        subtitle = "Track active orders & order history",
                        onClick = onNavigateToOrders,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Gold membership toggle
                    SettingsRow(
                        icon = Icons.Filled.Star,
                        iconBackground = if (isGoldMember) Color(0xFFFFF8E1) else MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = GoldColour,
                        title = "Gold Membership",
                        subtitle = if (isGoldMember) "Active — unlimited free delivery" else "Tap to activate premium plan",
                        modifier = Modifier
                            .then(
                                if (isGoldMember) Modifier.border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(listOf(GoldColour, Color(0xFFFFB300))),
                                    shape = RoundedCornerShape(0.dp)
                                ) else Modifier
                            ),
                        trailingContent = {
                            Switch(
                                checked = isGoldMember,
                                onCheckedChange = { onToggleGold() },
                                thumbContent = {
                                    if (isGoldMember) {
                                        Text("★", fontSize = 10.sp)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldColour,
                                    checkedIconColor = Color(0xFFFFB300)
                                )
                            )
                        }
                    )
                }
            }

            // ── FAMILY section ───────────────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "Family",
                    accentColor = PurpleAccent
                )
            }

            item {
                SettingsSectionCard {
                    SettingsRow(
                        icon = Icons.Outlined.Group,
                        iconBackground = Color(0xFFF3E5F5),
                        iconTint = PurpleAccent,
                        title = "Family Plan Management",
                        subtitle = "Share benefits with your family",
                        onClick = onNavigateToFamily,
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (familyMemberCount > 0) {
                                    FamilyMemberBadge(count = familyMemberCount)
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // ── PAYMENTS section ─────────────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "Payments",
                    accentColor = GreenAccent
                )
            }

            item {
                SettingsSectionCard {
                    // Saved UPI IDs
                    SettingsRow(
                        icon = Icons.Outlined.AccountBalance,
                        iconBackground = Color(0xFFE8F5E9),
                        iconTint = GreenAccent,
                        title = "Saved UPI IDs",
                        subtitle = "tharun@paytm  +1 more",
                        onClick = { showUpiDialog = true },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = GreenAccent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "2 saved",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Saved Cards
                    SettingsRow(
                        icon = Icons.Outlined.CreditCard,
                        iconBackground = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1E88E5),
                        title = "Saved Cards",
                        subtitle = "HDFC Visa  •••• 4242",
                        onClick = { /* open cards management */ },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── PREFERENCES section ──────────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "Preferences",
                    accentColor = Color(0xFF1E88E5)
                )
            }

            item {
                SettingsSectionCard {
                    // Theme toggle
                    SettingsRow(
                        icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        iconBackground = if (isDarkTheme) Color(0xFF1A237E) else Color(0xFFFFFDE7),
                        iconTint = if (isDarkTheme) Color(0xFF7986CB) else Color(0xFFFFB300),
                        title = if (isDarkTheme) "Dark Mode" else "Light Mode",
                        subtitle = "Tap to switch appearance",
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleTheme() },
                                thumbContent = {
                                    Text(
                                        text = if (isDarkTheme) "🌙" else "☀️",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF3F51B5),
                                    uncheckedTrackColor = Color(0xFFFFB300).copy(alpha = 0.4f)
                                )
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Order Updates notification
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        iconBackground = Color(0xFFFFF3E0),
                        iconTint = OrangeGradientStart,
                        title = "Order Updates",
                        subtitle = "Push alerts for your active orders",
                        trailingContent = {
                            Switch(
                                checked = orderUpdatesOn,
                                onCheckedChange = { orderUpdatesOn = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = OrangeGradientStart
                                )
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Promotions notification
                    SettingsRow(
                        icon = Icons.Outlined.LocalOffer,
                        iconBackground = Color(0xFFF3E5F5),
                        iconTint = PurpleAccent,
                        title = "Promotions & Offers",
                        subtitle = "Receive personalised deals and coupons",
                        trailingContent = {
                            Switch(
                                checked = promotionsOn,
                                onCheckedChange = { promotionsOn = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = PurpleAccent
                                )
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Language selector
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        iconBackground = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1E88E5),
                        title = "Language",
                        subtitle = "English (United States)",
                        onClick = { /* open language picker */ },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF1E88E5).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "EN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E88E5),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // ── PRIVACY & LEGAL section ──────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "Privacy & Legal",
                    accentColor = Color(0xFF607D8B)
                )
            }

            item {
                SettingsSectionCard {
                    // Privacy Policy
                    SettingsRow(
                        icon = Icons.Outlined.PrivacyTip,
                        iconBackground = Color(0xFFECEFF1),
                        iconTint = Color(0xFF546E7A),
                        title = "Privacy Policy",
                        subtitle = "How we handle your personal data",
                        onClick = { showLegalDialog = "Privacy Policy" },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Terms of Service
                    SettingsRow(
                        icon = Icons.Outlined.Gavel,
                        iconBackground = Color(0xFFECEFF1),
                        iconTint = Color(0xFF546E7A),
                        title = "Terms of Service",
                        subtitle = "User agreement and platform rules",
                        onClick = { showLegalDialog = "Terms of Service" },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Licenses
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.Article,
                        iconBackground = Color(0xFFECEFF1),
                        iconTint = Color(0xFF546E7A),
                        title = "Open Source Licenses",
                        subtitle = "Third-party library attributions",
                        onClick = { showLegalDialog = "Open Source Licenses" },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── ABOUT section ────────────────────────────────────────────────
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionHeader(
                    title = "About",
                    accentColor = OrangeGradientStart
                )
            }

            item {
                SettingsSectionCard {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        iconBackground = Color(0xFFFFF3E0),
                        iconTint = OrangeGradientStart,
                        title = "App Version",
                        subtitle = "2.0.0 Enterprise",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = OrangeGradientStart.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Up to date",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeGradientStart,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    SettingsRow(
                        icon = Icons.Outlined.Build,
                        iconBackground = Color(0xFFFFF3E0),
                        iconTint = OrangeGradientStart,
                        title = "Build",
                        subtitle = "Enterprise Edition",
                        trailingContent = {
                            Text(
                                text = "EC-2026",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── Logout button ────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(28.dp))
                LogoutButton()
                Spacer(modifier = Modifier.height(8.dp))
                // Footer brand text
                Text(
                    text = "BiteCraft • 2.0.0 Enterprise Edition",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    if (showUpiDialog) {
        SavedUpiDialog(onDismiss = { showUpiDialog = false })
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            initialName = userName,
            initialEmail = userEmail,
            initialPhone = userPhone,
            onSave = onSaveProfile,
            onDismiss = { showProfileDialog = false }
        )
    }

    showLegalDialog?.let { title ->
        LegalDialog(title = title, onDismiss = { showLegalDialog = null })
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. SettingsHeader — gradient hero with avatar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeader(isGoldMember: Boolean, onToggleGold: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(OrangeGradientStart, OrangeGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 180.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = (-40).dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(
                            width = if (isGoldMember) 2.5.dp else 2.dp,
                            color = if (isGoldMember) GoldColour else Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGoldMember) {
                        Text("👑", fontSize = 26.sp)
                    } else {
                        Text(
                            text = "TV",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tharun V",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (isGoldMember) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GoldColour.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "★ GOLD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldColour,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "tharun.v@bitecraft.app",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Member since Jan 2024",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Quick stats row inline
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛍️", fontSize = 16.sp)
                    Text("47", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Orders", fontSize = 9.sp, color = Color.White.copy(alpha = 0.75f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💰", fontSize = 16.sp)
                    Text("$128", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Saved", fontSize = 9.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun HeaderStatChip(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. SettingsSectionHeader
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsSectionHeader(
    title: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Coloured left border
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            letterSpacing = 1.2.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. SettingsSectionCard — card wrapper for groups of rows
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. SettingsRow — reusable row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing slot
        trailingContent?.invoke()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. FamilyMemberBadge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FamilyMemberBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PurpleAccent.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("👨‍👩‍👧", fontSize = 11.sp)
            Text(
                text = "$count members",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PurpleAccent
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. Logout button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton() {
    var showConfirm by remember { mutableStateOf(false) }

    Button(
        onClick = { showConfirm = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RedLogout.copy(alpha = 0.1f),
            contentColor = RedLogout
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, RedLogout.copy(alpha = 0.35f))
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = "Logout",
            tint = RedLogout,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Log Out",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RedLogout
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text("Log out?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You will be redirected to the sign-in screen. Your cart and saved preferences will be preserved.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showConfirm = false /* TODO: trigger actual logout */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = RedLogout)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. SavedUpiDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SavedUpiDialog(onDismiss: () -> Unit) {
    // Local state for the UPI IDs
    val upiIds = remember { mutableStateListOf("tharun@paytm", "user@gpay") }
    var newUpiText by remember { mutableStateOf("") }
    var newUpiError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    fun addUpi() {
        val trimmed = newUpiText.trim()
        when {
            trimmed.isEmpty() -> newUpiError = "UPI ID cannot be empty"
            !trimmed.contains("@") -> newUpiError = "Enter a valid UPI ID (e.g. name@bank)"
            upiIds.contains(trimmed) -> newUpiError = "This UPI ID is already saved"
            else -> {
                upiIds.add(trimmed)
                newUpiText = ""
                newUpiError = null
                focusManager.clearFocus()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Dialog header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(GreenAccent, Color(0xFF2E7D32))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "Saved UPI IDs",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "${upiIds.size} UPI IDs linked",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // List of saved UPI IDs
                    if (upiIds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💳", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No UPI IDs saved yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        upiIds.forEachIndexed { index, upiId ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(GreenAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalance,
                                                contentDescription = null,
                                                tint = GreenAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = upiId,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = upiId.substringAfter("@")
                                                    .replaceFirstChar { it.uppercase() } + " UPI",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        // Delete button
                                        IconButton(
                                            onClick = { upiIds.removeAt(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Remove $upiId",
                                                tint = RedLogout.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Spacer(modifier = Modifier.height(4.dp))

                    // Add new UPI field
                    Text(
                        text = "Add New UPI ID",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = newUpiText,
                        onValueChange = {
                            newUpiText = it
                            newUpiError = null
                        },
                        placeholder = {
                            Text(
                                "e.g. yourname@okicici",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Text("@", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = GreenAccent, modifier = Modifier.padding(start = 4.dp))
                        },
                        trailingIcon = {
                            if (newUpiText.isNotEmpty()) {
                                IconButton(onClick = { newUpiText = ""; newUpiError = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        isError = newUpiError != null,
                        supportingText = {
                            newUpiError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { addUpi() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            focusedLabelColor = GreenAccent
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { addUpi() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add UPI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 9. ProfileEditDialog — inline profile editor
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileEditDialog(
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    onSave: (name: String, email: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name  by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }

    val initials = remember(name) {
        if (name.isBlank()) "FD" else {
            name.split(" ")
                .filter { it.isNotEmpty() }
                .map { it[0].uppercaseChar() }
                .joinToString("")
                .take(2)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(OrangeGradientStart, OrangeGradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = OrangeGradientStart) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeGradientStart)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = OrangeGradientStart) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeGradientStart)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    enabled = false,
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = OrangeGradientStart) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeGradientStart,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, email)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeGradientStart)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 10. LegalDialog — privacy / terms / licenses viewer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LegalDialog(title: String, onDismiss: () -> Unit) {
    val bodyText = when (title) {
        "Privacy Policy" ->
            "BiteCraft respects your privacy. We collect only the data necessary to provide our food delivery service, including your name, email, phone number, and delivery address. Your data is never sold to third parties. You may request deletion of your account data at any time by contacting support@bitecraft.app.\n\nWe use industry-standard encryption for all data in transit and at rest. Cookie usage is limited to session management and preference storage only."
        "Terms of Service" ->
            "By using BiteCraft, you agree to our terms of service. Orders placed on the platform are subject to restaurant availability and rider capacity. Refunds are processed within 3–5 business days for eligible cancellations. Misuse of the platform, including fraudulent orders, may result in account suspension.\n\nBiteCraft Enterprise Edition is licensed for commercial and personal use. Redistribution or resale of the application binary is prohibited without written consent."
        else ->
            "BiteCraft is built using open-source libraries, each governed by their respective licenses:\n\n• Jetpack Compose — Apache 2.0\n• Coil — Apache 2.0\n• Room — Apache 2.0\n• Kotlin Coroutines — Apache 2.0\n• Leaflet.js — BSD-2-Clause\n• Material Icons — Apache 2.0\n• OkHttp — Apache 2.0\n\nFull license texts are available at bitecraft.app/licenses"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = bodyText,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
