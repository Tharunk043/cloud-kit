package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.GeminiClient
import com.example.data.local.CartItemEntity
import com.example.data.local.DishEntity
import com.example.data.local.OrderEntity
import com.example.data.local.RestaurantEntity
import com.example.viewmodel.PlatformViewModel
import com.example.ui.theme.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.location.Geocoder
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date

// --- Unsplash Image Fetchers ---

fun getRestaurantBannerUrl(imageName: String): String {
    return when(imageName) {
        "burger_banner" -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80"
        "pizza_banner" -> "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=800&q=80"
        "ramen_banner" -> "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=800&q=80"
        "dessert_banner" -> "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=800&q=80"
        else -> "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=800&q=80"
    }
}

fun getRestaurantImageUrl(imageName: String): String {
    return when(imageName) {
        "burger_lab" -> "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=300&q=80"
        "pizza_slice" -> "https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?auto=format&fit=crop&w=300&q=80"
        "ramen_bowl" -> "https://images.unsplash.com/photo-1557872943-16a5ac26437e?auto=format&fit=crop&w=300&q=80"
        "cake_slice" -> "https://images.unsplash.com/photo-1550617931-e17a7b70dce2?auto=format&fit=crop&w=300&q=80"
        else -> "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=300&q=80"
    }
}

fun getDishImageUrl(dishName: String): String {
    return when(dishName) {
        "Truffle Truce Burger" -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80"
        "Spicy BBQ Heat Burger" -> "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=400&q=80"
        "Crinkly Parm Fries" -> "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=400&q=80"
        "Cosmic Caramel Shake" -> "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80"
        "Burrata Blush Pizza" -> "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=400&q=80"
        "Fiery Diavola Pizza" -> "https://images.unsplash.com/photo-1590947132387-155cc02f3212?auto=format&fit=crop&w=400&q=80"
        "Creamy Mushroom Fettuccine" -> "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?auto=format&fit=crop&w=400&q=80"
        "Volcano Ramen Bowl" -> "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=400&q=80"
        "Sichuan Chili Glass Noodles" -> "https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=400&q=80"
        "Veggie Gyoza Platter" -> "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=400&q=80"
        "Luxe Lava Chocolate Fondant" -> "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=400&q=80"
        "Salted Caramel Pecan Cheesecake" -> "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?auto=format&fit=crop&w=400&q=80"
        "Matcha White Choc Scone" -> "https://images.unsplash.com/photo-1587314168485-3236d6710814?auto=format&fit=crop&w=400&q=80"
        else -> "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=400&q=80"
    }
}

// --- Navigation Destinations ---

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail")
    object Cart : Screen("cart")
    object Tracking : Screen("tracking")
    object Wallet : Screen("wallet")
    object Support : Screen("support")
    object Settings : Screen("settings")
    object Family : Screen("family")
    object OrdersHistory : Screen("orders_history")
}

@Composable
fun SplashScreenView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Accent orange circle with Fastfood logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF9800), Color(0xFFFC8019))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fastfood,
                    contentDescription = "BiteCraft Splash",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Brand Headline
            Text(
                text = "BiteCraft",
                color = Color(0xFFFC8019),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Tagline
            Text(
                text = "Fast Delivery • Fresh Food • AI Powered",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Minimal Loading Dot Indicator
            CircularProgressIndicator(
                color = Color(0xFFFC8019),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Recreated with Swiggy Free Kit • Community",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// --- Shared Elements (Role Switching Bar) ---

@Composable
fun RoleSwitcherBar(
    currentRole: String,
    onRoleSelected: (String) -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BiteCraft",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Switcher Button
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("theme_toggle")
                    ) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            fontSize = 18.sp
                        )
                    }

                    // Small pill showing active key status for transparency
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (GeminiClient.isApiKeyConfigured()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (GeminiClient.isApiKeyConfigured()) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (GeminiClient.isApiKeyConfigured()) "Gemini Live" else "Demo Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (GeminiClient.isApiKeyConfigured()) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            // Role selectors
            val roles = listOf("Customer", "Kitchen", "Rider", "Admin")
            ScrollableTabRow(
                selectedTabIndex = roles.indexOf(currentRole).coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {},
                containerColor = Color.Transparent,
                indicator = {}
            ) {
                roles.forEach { role ->
                    val selected = currentRole == role
                    Tab(
                        selected = selected,
                        onClick = { onRoleSelected(role) },
                        modifier = Modifier.testTag("role_tab_${role.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when(role) {
                                    "Customer" -> Icons.Outlined.Person
                                    "Kitchen" -> Icons.Outlined.Storefront
                                    "Rider" -> Icons.Outlined.LocalShipping
                                    else -> Icons.Outlined.AdminPanelSettings
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = role,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = role,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. CUSTOMER APPLICATION VIEWS
// ==========================================

@Composable
fun ActiveOrderBanner(
    order: OrderEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .testTag("active_order_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing Emoji representing current status
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val orderIconRes = when (order.status) {
                    "Placed" -> com.example.R.drawable.ic_service_genie
                    "Accepted" -> com.example.R.drawable.ic_wallet_cashback
                    "Preparing" -> com.example.R.drawable.ic_category_ramen
                    "OutForDelivery" -> com.example.R.drawable.ic_category_burger
                    else -> com.example.R.drawable.ic_category_burger
                }
                Image(
                    painter = painterResource(id = orderIconRes),
                    contentDescription = order.status,
                    modifier = Modifier.size(24.dp).graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BiteCraft Active Delivery",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (order.status) {
                        "Placed" -> "Order placed! Awaiting kitchen confirmation..."
                        "Accepted" -> "Restaurant accepted. Cooking soon..."
                        "Preparing" -> "Chef is preparing your gourmet meal..."
                        "OutForDelivery" -> "Rider is out for delivery! Heading your way..."
                        else -> "Order status: ${order.status}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Track Live",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerSection(
    viewModel: PlatformViewModel,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenFamily: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            Column {
                val activeId by viewModel.activeOrderId.collectAsState()
                val orders by viewModel.orders.collectAsState()
                val activeOrder = orders.find { it.id == activeId }
                if (activeOrder != null && currentScreen != Screen.Tracking && activeOrder.status != "Delivered" && activeOrder.status != "Cancelled") {
                    ActiveOrderBanner(
                        order = activeOrder,
                        onClick = { onNavigate(Screen.Tracking) }
                    )
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val items = listOf(
                        Triple(Screen.Home, Icons.Outlined.Home, "Explore"),
                        Triple(Screen.Cart, Icons.Outlined.ShoppingCart, "Cart"),
                        Triple(Screen.Wallet, Icons.Outlined.AccountBalanceWallet, "Wallet")
                    )
                    items.forEach { (screen, icon, label) ->
                        val selected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(screen) },
                            icon = {
                                BadgedBox(badge = {
                                    if (screen == Screen.Cart && viewModel.cartItems.collectAsState().value.isNotEmpty()) {
                                        Badge {
                                            Text(viewModel.cartItems.collectAsState().value.sumOf { it.quantity }.toString())
                                        }
                                    }
                                }) {
                                    Icon(icon, contentDescription = label)
                                }
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                    // Settings item
                    NavigationBarItem(
                        selected = false,
                        onClick = { onOpenSettings() },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                is Screen.Home -> ExploreView(viewModel, onNavigate, onOpenSettings)
                is Screen.Detail -> RestaurantDetailView(viewModel, onNavigate)
                is Screen.Cart -> ClientCartView(viewModel, onNavigate)
                is Screen.Tracking -> ActiveOrderTrackingView(viewModel, onNavigate)
                is Screen.Wallet -> ClientWalletView(viewModel, onOpenFamily = onOpenFamily)
                is Screen.OrdersHistory -> OrdersHistoryView(
                    viewModel = viewModel,
                    onNavigate = onNavigate,
                    onBack = {
                        onNavigate(Screen.Home)
                        onOpenSettings()
                    }
                )
                else -> ExploreView(viewModel, onNavigate, onOpenSettings)
            }
        }
    }
}

@Composable
fun ExploreView(
    viewModel: PlatformViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit
) {
    val search by viewModel.searchQuery.collectAsState()
    val category by viewModel.activeCategory.collectAsState()
    val restaurantsList by viewModel.filteredRestaurants.collectAsState()
    val isGold by viewModel.isGoldMember.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLocatingHome by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    // Permission launcher — opens location picker after permission granted
    val homeLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            showLocationPicker = true
        } else {
            Toast.makeText(context, "Location permission required to use map picker.", Toast.LENGTH_SHORT).show()
        }
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var pendingAddressInfo by remember { mutableStateOf<Triple<String, Double, Double>?>(null) }

    // Address Management Dialog
    if (showAddressDialog) {
        AddressManagementDialog(
            viewModel = viewModel,
            onDismiss = { showAddressDialog = false },
            onAddNewAddress = {
                showAddressDialog = false
                homeLocationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }

    // Address Label Dialog
    pendingAddressInfo?.let { info ->
        AddressLabelDialog(
            address = info.first,
            onConfirm = { label ->
                viewModel.userAddress.value = info.first
                viewModel.updateDeviceLocation(info.second, info.third)
                viewModel.saveDeliveryAddress(label, info.first, info.second, info.third)
                pendingAddressInfo = null
                Toast.makeText(context, "📍 Delivery location saved as $label!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                pendingAddressInfo = null
            }
        )
    }

    // Location picker sheet — real OSM map
    if (showLocationPicker) {
        val devLat = viewModel.deviceLatitude.collectAsState().value ?: 12.9716
        val devLng = viewModel.deviceLongitude.collectAsState().value ?: 77.5946
        OSMLocationPickerSheet(
            initialLat = devLat,
            initialLng = devLng,
            onAddressSelected = { address, lat, lng ->
                pendingAddressInfo = Triple(address, lat, lng)
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false }
        )
    }

    // AI Mood fields
    var moodInput by remember { mutableStateOf("") }
    val aiRecommend by viewModel.aiRecommendationResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Upper section with Location search & Member banner
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Address locator - tappable for GPS location update
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    // Clickable location area (icon + address column) — opens real map picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAddressDialog = true
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        if (isLocatingHome) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Tap to update location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isLocatingHome) "Detecting location..." else "Delivery to",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!isLocatingHome) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = userAddress,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Dark theme toggle
                    val isDark by viewModel.isDarkTheme.collectAsState()
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.padding(end = 4.dp).testTag("header_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // User gold active tag or initials avatar
                    val currentName by viewModel.currentUserName.collectAsState()
                    val initials = remember(currentName) {
                        if (currentName.isBlank()) "FD" else {
                            currentName.split(" ")
                                .filter { it.isNotEmpty() }
                                .map { it[0].uppercaseChar() }
                                .joinToString("")
                                .take(2)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGold) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGold) {
                            Text("👑", fontSize = 18.sp)
                        } else {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Swiggy One / BiteCraft Gold Custom banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isGold) listOf(Color(0xFF232526), Color(0xFF414345))
                                         else listOf(Color(0xFFFF5722), Color(0xFFFF9800))
                            )
                        )
                        .clickable { viewModel.toggleGoldMembership() }
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGold) "★ BITECRAFT GOLD ACTIVE" else "Join BiteCraft Gold",
                                color = if (isGold) Color(0xFFFFD54F) else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isGold) "Unlimited Free Delivery + 10% instant wallet cashbacks configured."
                                       else "Unlock zero delivery fees above $12.00. Get $1.00 cashback stats.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.toggleGoldMembership() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGold) Color(0xFFFFD54F) else Color.White,
                                contentColor = if (isGold) Color.Black else Color(0xFFFF5722)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isGold) "Cancel" else "Join", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Dynamic Search block with voice mic style
                OutlinedTextField(
                    value = search,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { 
                        Text(
                            text = "Search 'burgers', 'gourmet pizza' or 'desserts'...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("🎤", fontSize = 18.sp, modifier = Modifier.clickable { /* mic action */ })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .testTag("search_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }
        }

        // Swiggy Service Verticals Directory Quick Grid
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Service 1: Food Delivery (Vibrant Orange)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                                )
                            )
                            .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.activeCategory.value = "All"
                            }
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_category_burger),
                            contentDescription = "Food Delivery",
                            modifier = Modifier
                                .size(74.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFC8019))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("CRAVING", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Food\nDelivery", fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 18.sp, color = Color(0xFF5D4037))
                            Text("UP TO 50% OFF", fontSize = 9.sp, color = Color(0xFFFC8019), fontWeight = FontWeight.Black)
                        }
                    }

                    // Service 2: Instamart (Emerald Fresh Green)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                )
                            )
                            .border(1.dp, Color(0xFF81C784).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.activeCategory.value = "Healthy"
                            }
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_service_instamart),
                            contentDescription = "Instamart",
                            modifier = Modifier
                                .size(74.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2E7D32))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("10 MINS", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Instamart\nGroceries", fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 18.sp, color = Color(0xFF1B5E20))
                            Text("FRESH ITEMS", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Service 3: Dineout (Luxury Sky Blue)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                                )
                            )
                            .border(1.dp, Color(0xFF64B5F6).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.activeCategory.value = "All"
                            }
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_service_dineout),
                            contentDescription = "Dineout",
                            modifier = Modifier
                                .size(74.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E88E5))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("RESERVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Dineout\nTables", fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 18.sp, color = Color(0xFF0D47A1))
                            Text("FLAT 30% OFF", fontSize = 9.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Black)
                        }
                    }

                    // Service 4: Genie (Majestic Lilac Purple)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))
                                )
                            )
                            .border(1.dp, Color(0xFFBA68C8).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.activeCategory.value = "All"
                            }
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_service_genie),
                            contentDescription = "Genie Courier",
                            modifier = Modifier
                                .size(74.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF8E24AA))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ANYTHING", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Genie\nCourier", fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 18.sp, color = Color(0xFF4A148C))
                            Text("SEND PARCEL", fontSize = 9.sp, color = Color(0xFF8E24AA), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Active promo banner carousel
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Trending Offers For You",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
                
                val promos = listOf(
                    Triple("60% OFF up to $10", "Use code FEAST60 • On all Pizza, Burgers & Desserts!", listOf(Color(0xFFFF7043), Color(0xFFE64A19))),
                    Triple("Flat $5 Discount", "Use code PAYDAYSIN • Minimum order value $15.", listOf(Color(0xFF26A69A), Color(0xFF00796B))),
                    Triple("BiteCraft Gold Free Delivery", "Zero shipping fee configured on premium kitchen menus.", listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)))
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promos) { (headline, body, gradientColors) ->
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(95.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(colors = gradientColors))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                Text(headline, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text(body, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, maxLines = 2, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // What's on your mind section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "What's on your mind?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
                
                val dbCategoriesState by viewModel.categories.collectAsState()
                val dbCategories = if (dbCategoriesState.isNotEmpty()) dbCategoriesState else listOf(
                    com.example.data.local.CategoryEntity("burgers", "Burgers", "ic_category_burger", "#FFFFEAD2"),
                    com.example.data.local.CategoryEntity("pizza", "Pizza", "ic_category_pizza", "#FFFFF1EB"),
                    com.example.data.local.CategoryEntity("sushi", "Sushi", "ic_category_sushi", "#FFE4F0EC"),
                    com.example.data.local.CategoryEntity("healthy", "Healthy", "ic_category_healthy", "#FFE2F3E7"),
                    com.example.data.local.CategoryEntity("desserts", "Desserts", "ic_category_dessert", "#FFFFF0F5"),
                    com.example.data.local.CategoryEntity("ramen", "Ramen", "ic_category_ramen", "#FFFFEAD2")
                )
                val context = LocalContext.current
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(dbCategories) { index, catEntity ->
                        val title = catEntity.name
                        val isSelected = category == title
                        
                        val drawableId = remember(catEntity.imageResName) {
                            val id = context.resources.getIdentifier(catEntity.imageResName, "drawable", context.packageName)
                            if (id != 0) id else com.example.R.drawable.ic_category_burger
                        }
                        
                        val parsedColor = remember(catEntity.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(catEntity.colorHex))
                            } catch (e: Exception) {
                                CategoryBurger
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    if (isSelected) {
                                        viewModel.activeCategory.value = "All"
                                    } else {
                                        viewModel.activeCategory.value = title
                                    }
                                }
                                .testTag("mind_category_${title.lowercase()}")
                        ) {
                            Card(
                                modifier = Modifier.size(76.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFEEEEEE)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = title,
                                        modifier = Modifier.size(58.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Category Tabs
        item {
            val categorTabs = listOf("All", "Veg Only", "Promoted")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(categorTabs) { tab ->
                    val active = category == tab
                    FilterChip(
                        selected = active,
                        onClick = { viewModel.activeCategory.value = tab },
                        label = { Text(tab, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${tab.replace(" ", "_").lowercase()}")
                    )
                }
            }
        }

        // Gourmet AI Assistant recommendations block (using Gemini)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BiteCraft AI Mood Planner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Feeling down, tired or celebrating? Describe your vibe & let Gemini script your perfect menu!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = moodInput,
                            onValueChange = { moodInput = it },
                            placeholder = { Text("e.g. Rainy Sunday dessert, high energy cheat burger") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_mood_input"),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (moodInput.isNotEmpty()) {
                                    viewModel.runAiMoodRecommendation(moodInput)
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                                .testTag("ai_mood_btn")
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Query", tint = Color.White)
                            }
                        }
                    }

                    if (aiRecommend.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiRecommend,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Restaurant Lists
        item {
            Text(
                text = "Premium Cloud Kitchens Nearby",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Search result count badge when search is active
        if (search.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (restaurantsList.isEmpty()) "No results for \"${search}\"" else "Showing ${restaurantsList.size} restaurant${if (restaurantsList.size != 1) "s" else ""} for \"${search}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Text("Clear", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (restaurantsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (search.isNotEmpty()) Icons.Default.SearchOff else Icons.Outlined.Storefront,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (search.isNotEmpty()) {
                            Text(
                                text = "No results for \"${search}\"",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try different keywords or clear the search.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Search")
                            }
                        } else {
                            Text("No matching kitchens found. Modify filters!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            items(restaurantsList) { restaurant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            viewModel.selectedRestaurantId.value = restaurant.id
                            onNavigate(Screen.Detail)
                        }
                        .testTag("restaurant_card_${restaurant.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Hero Image of Restaurant
                        Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                            AsyncImage(
                                model = getRestaurantBannerUrl(restaurant.bannerImage),
                                contentDescription = restaurant.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Promoted Tag
                            if (restaurant.isPromoted) {
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text("PROMOTED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Delivery time duration pill
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text("${restaurant.deliveryTime} mins", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Info Section below image in premium Swiggy-like layout
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = restaurant.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (restaurant.isVeg) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .border(1.dp, Color(0xFF4CAF50))
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = restaurant.cuisine,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Beautiful solid green rating badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, contentDescription = "Rating", modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(restaurant.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.AccessTime, contentDescription = "Time", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${restaurant.deliveryTime} mins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(Icons.Outlined.LocationOn, contentDescription = "Distance", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${restaurant.distanceKm} km away", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Text(
                                    text = if (restaurant.deliveryFee == 0.0) "FREE Delivery" else "$${restaurant.deliveryFee} Delivery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (restaurant.deliveryFee == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Dynamic offer tag inside card matching Swiggy's UX!
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Promo discount offer",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (restaurant.isPromoted) "50% OFF up to $10 • Code WELCOME50" else "Flat 20% discount with BiteCraft Gold",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
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

// --- RESTAURANT DETAIL & DISH VIEW ---

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailView(
    viewModel: PlatformViewModel,
    onNavigate: (Screen) -> Unit
) {
    val restId = viewModel.selectedRestaurantId.collectAsState().value ?: -1
    val rests by viewModel.rawRestaurants.collectAsState()
    val restaurant = rests.find { it.id == restId } ?: return

    val dishes by viewModel.getDishesForSelectedRestaurant().collectAsState()
    val cart by viewModel.cartItems.collectAsState()
    
    // Customize Dialog State
    var selectedDishForCustomization by remember { mutableStateOf<DishEntity?>(null) }
    val spice by viewModel.selectedSpiceLevel.collectAsState()
    val selectedAddons by viewModel.selectedAddons.collectAsState()
    val notes by viewModel.customNotes.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Back toolbar using proper TopAppBar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            // Expanded detail body
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp) // extra padding so list isn't cut off by floating bar
            ) {
                // Header Image/Profile Card
                item {
                    Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                        AsyncImage(
                            model = getRestaurantBannerUrl(restaurant.bannerImage),
                            contentDescription = restaurant.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
                        
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(restaurant.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(restaurant.description, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Quick Stats Block
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rating", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(restaurant.rating.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Delivery", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${restaurant.deliveryTime} mins", fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Charge", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (restaurant.deliveryFee == 0.0) "Free" else "$${restaurant.deliveryFee}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // List Dishes Category by Category
                val categories = dishes.map { it.category }.distinct()
                categories.forEach { categoryName ->
                    item {
                        Text(
                            text = categoryName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(dishes.filter { it.category == categoryName }) { dish ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val symbolColor = if (dish.isVeg) Color(0xFF4CAF50) else Color(0xFFE53935)
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .border(1.dp, symbolColor)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(symbolColor))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (dish.isBestseller) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("BESTSELLER", color = MaterialTheme.colorScheme.secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(dish.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("$${dish.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dish.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }

                            // Dish Visual Card & Overlapping Add Button matching Swiggy Premium layout
                            Box(
                                modifier = Modifier
                                    .width(115.dp)
                                    .height(125.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Image Card
                                Card(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(105.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    AsyncImage(
                                        model = getDishImageUrl(dish.name),
                                        contentDescription = dish.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                // Overlapping ADD Button or Counter Selector
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                ) {
                                    val cartItem = cart.find { it.dishId == dish.id && it.restaurantId == restaurant.id }
                                    if (cartItem != null) {
                                        // Count switcher - / +
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 6.dp,
                                            shadowElevation = 4.dp,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(34.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clickable {
                                                            viewModel.modifyCartQuantity(cartItem.id, cartItem.quantity, false)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, textAlign = TextAlign.Center)
                                                }
                                                Text(
                                                    text = cartItem.quantity.toString(),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 13.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clickable {
                                                            viewModel.modifyCartQuantity(cartItem.id, cartItem.quantity, true)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, textAlign = TextAlign.Center)
                                                }
                                            }
                                        }
                                    } else {
                                        // ADD button that triggers customization dialog (if choice supports spice / addons)
                                        Button(
                                            onClick = {
                                                selectedDishForCustomization = dish
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 5.dp,
                                                pressedElevation = 2.dp
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            contentPadding = PaddingValues(horizontal = 12.dp),
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(34.dp)
                                                .testTag("add_dish_${dish.id}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "ADD",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "+",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
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
        }

        // Persistent Floating Swiggy Orange Cart Bar sliding at the bottom of Detail View
        if (cart.isNotEmpty()) {
            val totalCount = cart.sumOf { it.quantity }
            val totalPrice = cart.sumOf { it.price * it.quantity }
            
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigate(Screen.Cart) }
                    .testTag("floating_cart_bar"),
                color = Color(0xFFFC8019), // Swiggy Premium Orange
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$totalCount item${if (totalCount > 1) "s" else ""} • $${String.format("%.2f", totalPrice)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "plus taxes",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Cart",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Customization Bottom Sheet (Slides up cleanly - No annoying popup dialog!)
    selectedDishForCustomization?.let { dish ->
        ModalBottomSheet(
            onDismissRequest = { selectedDishForCustomization = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Title and Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize " + dish.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${dish.price}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFC8019),
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = { selectedDishForCustomization = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Spice level
                    if (dish.spiceLevelSupport) {
                        Text(
                            text = "Select Spice Level",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Mild", "Medium", "Spicy").forEach { level ->
                                val active = spice == level
                                FilterChip(
                                    selected = active,
                                    onClick = { viewModel.selectedSpiceLevel.value = level },
                                    label = { Text(level, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFC8019).copy(alpha = 0.15f),
                                        selectedLabelColor = Color(0xFFFC8019),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Addons Options parsing
                    val addonsList = dish.addonsJson.split(",")
                    if (addonsList.isNotEmpty() && addonsList.first().isNotEmpty()) {
                        Text(
                            text = "Choose Premium Add-ons",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        addonsList.forEach { addon ->
                            val active = selectedAddons.contains(addon)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.selectedAddons.value = if (active) selectedAddons - addon
                                                                         else selectedAddons + addon
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = active,
                                    onCheckedChange = {
                                        viewModel.selectedAddons.value = if (active) selectedAddons - addon
                                                                         else selectedAddons + addon
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFC8019),
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = addon,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Notes
                    Text(
                        text = "Kitchen Instructions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.customNotes.value = it },
                        placeholder = { Text("e.g. Please make it extra hot, no tomatoes", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFC8019),
                            cursorColor = Color(0xFFFC8019)
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Confirm Action Button (Large, modern & stylish orange bar)
                Button(
                    onClick = {
                        viewModel.addToCart(dish, 1)
                        selectedDishForCustomization = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_customization")
                ) {
                    Text(
                        text = "Add to Basket",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun fetchCurrentLocation(
    context: Context,
    onSuccess: (Double, Double) -> Unit,
    onFailure: () -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onSuccess(loc.latitude, loc.longitude)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onSuccess(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                onFailure()
                            }
                        }
                        .addOnFailureListener {
                            onFailure()
                        }
                }
            }
            .addOnFailureListener {
                onFailure()
            }
    } catch (e: java.lang.Exception) {
        onFailure()
    }
}

// --- CART & SECURE CHECKOUT FLOW ---

@Composable
fun ClientCartView(
    viewModel: PlatformViewModel,
    onNavigate: (Screen) -> Unit
) {
    val cart by viewModel.cartItems.collectAsState()
    val isGold by viewModel.isGoldMember.collectAsState()
    val scope = rememberCoroutineScope()

    val activeAddress by viewModel.userAddress.collectAsState()
    var checkoutAddress by remember(activeAddress) { mutableStateOf(activeAddress) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // UPI, Credit Card, Wallet, COD
    val walletBal by viewModel.walletBalance.collectAsState()

    // --- PAYMENT SANDBOX STATES ---
    var showPaymentSandbox by remember { mutableStateOf(false) }
    var sandboxPaymentState by remember { mutableStateOf("IDLE") } // IDLE, PROCESSING, OTP_PENDING, SUCCESS, ERROR
    var sandboxOtpInput by remember { mutableStateOf("") }
    
    // Credit Card specific sandbox state
    var cardNumberInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    var cardNameInput by remember { mutableStateOf("") }
    
    // UPI specific sandbox state
    var upiVpaInput by remember { mutableStateOf("") }
    var upiPinInput by remember { mutableStateOf("") }
    var isPushNotificationVisible by remember { mutableStateOf(false) }

    var showUpiSheet by remember { mutableStateOf(false) }
    var showCardSheet by remember { mutableStateOf(false) }
    var showCodSheet by remember { mutableStateOf(false) }
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var successAmount by remember { mutableStateOf(0.0) }
    var successOrderId by remember { mutableStateOf("") }

    val devLat by viewModel.deviceLatitude.collectAsState()
    val devLng by viewModel.deviceLongitude.collectAsState()
    val subtotal = cart.sumOf { it.price * it.quantity }
    val delivery = if (isGold && subtotal > 12.0) 0.0 else 2.50
    val totalOrderAmt = subtotal + delivery - discountAmount(isGold, subtotal)

    val context = LocalContext.current
    var isLoadingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isLoadingLocation = true
            fetchCurrentLocation(
                context = context,
                onSuccess = { lat, lng ->
                    viewModel.updateDeviceLocation(lat, lng)
                    scope.launch(Dispatchers.IO) {
                        var resolvedAddress = "Real-Time GPS Lat: ${String.format(java.util.Locale.US, "%.5f", lat)}, Lng: ${String.format(java.util.Locale.US, "%.5f", lng)}"
                        try {
                            if (Geocoder.isPresent()) {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(lat, lng, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val addressLine = address.getAddressLine(0)
                                    if (!addressLine.isNullOrEmpty()) {
                                        resolvedAddress = addressLine
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ClientCartView", "Geocoding failed", e)
                        }
                        withContext(Dispatchers.Main) {
                            isLoadingLocation = false
                            checkoutAddress = resolvedAddress
                            Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = {
                    isLoadingLocation = false
                    Toast.makeText(context, "Using simulated GPS coordinates (location unavailable).", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permissions are required to auto-locate.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Your Basket",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Empty", modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your cart is empty. Add yummy things!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNavigate(Screen.Home) }) {
                        Text("Add Foods")
                    }
                }
            }
        } else {
            // Cart layout scrolling list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restaurant reference header
                item {
                    Text("Ordering from: ${cart.first().restaurantName}", fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                items(cart) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (item.spiceLevel.isNotEmpty() || item.addons.isNotEmpty()) {
                                Text("${item.spiceLevel} | ${item.addons}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("$${item.price * item.quantity}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }

                        // Qty increment/decrement
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.modifyCartQuantity(item.id, item.quantity, false) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                            }
                            Text(item.quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(onClick = { viewModel.modifyCartQuantity(item.id, item.quantity, true) }) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Billing breakdown
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        val subtotal = cart.sumOf { it.price * it.quantity }
                        val delivery = if (isGold && subtotal > 12.0) 0.0 else 2.50
                        val refundLoyalty = if (isGold) subtotal * 0.10 else 0.0
                        val total = subtotal + delivery - discountAmount(isGold, subtotal)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Basket Subtotal", fontSize = 12.sp)
                            Text("$${String.format("%.2f", subtotal)}", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("BiteCraft Delivery Fee", fontSize = 12.sp)
                            Text(if (delivery == 0.0) "FREE (Gold Benefit)" else "$${String.format("%.2f", delivery)}", fontSize = 12.sp)
                        }
                        if (isGold) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gold Cashback Discount", fontSize = 12.sp, color = Color(0xFFC69800))
                                Text("-$${String.format("%.2f", discountAmount(isGold, subtotal))}", fontSize = 12.sp, color = Color(0xFFC69800))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.ExtraBold)
                            Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                        }
                    }
                }

                // Delivery address info with locate me button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivery Address", fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            enabled = !isLoadingLocation,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFC8019))
                        ) {
                            if (isLoadingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFFC8019))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Locating...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = "Locate", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Locate Me (Real GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = checkoutAddress,
                        onValueChange = { checkoutAddress = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFC8019),
                            cursorColor = Color(0xFFFC8019)
                        )
                    )
                }

                // Payment selector
                item {
                    Text("Select Secure Payment Method", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    listOf("UPI", "Credit Card", "Wallet", "COD").forEach { method ->
                        val active = selectedPaymentMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = method }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = active,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFC8019))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (method == "Wallet") "BiteCraft Wallet (Balance: $${String.format("%.2f", walletBal)})" else method,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Checkout trigger button
                item {
                    Button(
                        onClick = {
                            if (selectedPaymentMethod == "Wallet" && walletBal < totalOrderAmt) {
                                Toast.makeText(context, "Insufficient balance! Please load money in BiteCraft Wallet first.", Toast.LENGTH_LONG).show()
                            } else {
                                when (selectedPaymentMethod) {
                                    "UPI" -> showUpiSheet = true
                                    "Credit Card" -> showCardSheet = true
                                    "COD" -> showCodSheet = true
                                    "Wallet" -> {
                                        sandboxPaymentState = "PROCESSING"
                                        showPaymentSandbox = true
                                        scope.launch {
                                            kotlinx.coroutines.delay(1500)
                                            sandboxPaymentState = "SUCCESS"
                                            kotlinx.coroutines.delay(1000)
                                            viewModel.placeOrder(selectedPaymentMethod, checkoutAddress, devLat, devLng) { orderId ->
                                                showPaymentSandbox = false
                                                if (orderId > 0) {
                                                    successAmount = totalOrderAmt
                                                    successOrderId = orderId.toString()
                                                    showSuccessOverlay = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(52.dp)
                            .testTag("checkout_btn"),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
                    ) {
                        Text("PLACE SECURE ORDER", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (showPaymentSandbox) {
        val scope = rememberCoroutineScope()
        Dialog(onDismissRequest = { 
            if (sandboxPaymentState != "PROCESSING") showPaymentSandbox = false 
        }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BiteCraft Pay Sandbox",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showPaymentSandbox = false },
                            enabled = sandboxPaymentState != "PROCESSING"
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Sandbox")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    if (sandboxPaymentState == "PROCESSING") {
                        Spacer(modifier = Modifier.height(30.dp))
                        CircularProgressIndicator(
                            color = Color(0xFFFC8019),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Securing simulated channel connection...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    } else if (sandboxPaymentState == "SUCCESS") {
                        Spacer(modifier = Modifier.height(30.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Test Charge Authorized!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            "Deducting sandbox ledger and placing order...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    } else if (sandboxPaymentState == "OTP_PENDING") {
                        Text(
                            "BiteCraft Secure Bank Authorization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "We sent a simulated OTP code to verify your Card Charge of $${String.format("%.2f", totalOrderAmt)}",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = sandboxOtpInput,
                            onValueChange = { sandboxOtpInput = it },
                            label = { Text("6-Digit Test OTP Code") },
                            placeholder = { Text("e.g. 123456") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("sandbox_otp_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { sandboxOtpInput = "123456" },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("AUTO-FILL OTP", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (sandboxOtpInput.length >= 4) {
                                        sandboxPaymentState = "PROCESSING"
                                        scope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            sandboxPaymentState = "SUCCESS"
                                            kotlinx.coroutines.delay(1000)
                                            viewModel.placeOrder(selectedPaymentMethod, checkoutAddress, devLat, devLng) { orderId ->
                                                showPaymentSandbox = false
                                                if (orderId > 0) {
                                                    onNavigate(Screen.Tracking)
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter a valid OTP code.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("SUBMIT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        when (selectedPaymentMethod) {
                            "Credit Card" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "SIMULATED TEST CARD",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                Icons.Default.CreditCard,
                                                contentDescription = "Contactless",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text(
                                            text = if (cardNumberInput.isEmpty()) "•••• •••• •••• ••••" else cardNumberInput,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("CARDHOLDER", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                                Text(
                                                    text = if (cardNameInput.isEmpty()) "YOUR NAME" else cardNameInput.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Column {
                                                    Text("EXPIRES", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                                    Text(
                                                        text = if (cardExpiryInput.isEmpty()) "MM/YY" else cardExpiryInput,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Column {
                                                    Text("CVV", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                                    Text(
                                                        text = if (cardCvvInput.isEmpty()) "•••" else cardCvvInput,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = cardNumberInput,
                                    onValueChange = { newVal ->
                                        if (newVal.length <= 19) {
                                            val digits = newVal.filter { it.isDigit() }
                                            val formatted = StringBuilder()
                                            for (i in digits.indices) {
                                                if (i > 0 && i % 4 == 0) {
                                                    formatted.append(" ")
                                                }
                                                formatted.append(digits[i])
                                            }
                                            cardNumberInput = formatted.toString()
                                        }
                                    },
                                    label = { Text("Card Number") },
                                    placeholder = { Text("4242 4242 4242 4242") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cardExpiryInput,
                                        onValueChange = { if (it.length <= 5) cardExpiryInput = it },
                                        label = { Text("Expiry") },
                                        placeholder = { Text("12/28") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = cardCvvInput,
                                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) cardCvvInput = it },
                                        label = { Text("CVV") },
                                        placeholder = { Text("123") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = cardNameInput,
                                    onValueChange = { cardNameInput = it },
                                    label = { Text("Cardholder Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "💡 STRIPE SANDBOX MODE: Use test card number `4242 4242 4242 4242` with any expiry in the future to simulate visa authorization card approval.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            cardNumberInput = "4242 4242 4242 4242"
                                            cardExpiryInput = "12/32"
                                            cardCvvInput = "424"
                                            cardNameInput = "Jane Doe"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("FAST FILL", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            if (cardNumberInput.length >= 15 && cardExpiryInput.isNotEmpty() && cardCvvInput.length == 3) {
                                                sandboxPaymentState = "PROCESSING"
                                                scope.launch {
                                                    kotlinx.coroutines.delay(1200)
                                                    sandboxPaymentState = "OTP_PENDING"
                                                }
                                            } else {
                                                Toast.makeText(context, "Please enter valid credit card particulars.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                                        enabled = cardNumberInput.isNotEmpty(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PAY $${String.format("%.2f", totalOrderAmt)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "UPI" -> {
                                Text(
                                    "Simulating Instant UPI Banking Channels",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = upiVpaInput,
                                    onValueChange = { upiVpaInput = it },
                                    label = { Text("UPI Virtual Payment Address (VPA)") },
                                    placeholder = { Text("name@bankhandle") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("upi_vpa_field")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("@okaxis", "@okicici", "@paytm", "@fbl").forEach { handle ->
                                        AssistChip(
                                            onClick = {
                                                val prefix = if (upiVpaInput.contains("@")) upiVpaInput.substringBefore("@") else "bitecraft"
                                                upiVpaInput = prefix + handle
                                            },
                                            label = { Text(handle, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (isPushNotificationVisible) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { isPushNotificationVisible = false }
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.PhoneAndroid,
                                                    contentDescription = "Notify",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "UPI BANK TRANSACTION PROMPT",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Tap here to authorize simulated debit of $${String.format("%.2f", totalOrderAmt)} to BiteCraft.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val upiUri = Uri.parse(
                                                    "upi://pay?pa=tharunvelamakuru143-3@okaxis&pn=Tharun%20Velamakuru&tn=BiteCraft%20Order&am=${String.format(java.util.Locale.US, "%.2f", totalOrderAmt)}&cu=INR"
                                                )
                                                val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                                context.startActivity(upiIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "No real UPI client (GPay/PhonePe/Paytm) found. Falling back to UPI Sandbox PIN approval.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                isPushNotificationVisible = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = "QR", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("LAUNCH UPI INTERNALS (TEST)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (!upiVpaInput.contains("@") || upiVpaInput.length < 5) {
                                                Toast.makeText(context, "Please enter a valid simulated VPA.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            sandboxPaymentState = "PROCESSING"
                                            scope.launch {
                                                kotlinx.coroutines.delay(1200)
                                                sandboxPaymentState = "OTP_PENDING"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("FAST SANDBOX PIN APPROVAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "Wallet" -> {
                                Text(
                                    "BiteCraft Account Wallet Settlement",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current Balance:", fontSize = 13.sp)
                                    Text("$${String.format("%.2f", walletBal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order Charges Amount:", fontSize = 13.sp)
                                    Text("-$${String.format("%.2f", totalOrderAmt)}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remaining Sandbox Balance:", fontSize = 13.sp)
                                    Text("$${String.format("%.2f", walletBal - totalOrderAmt)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        sandboxPaymentState = "PROCESSING"
                                        scope.launch {
                                            kotlinx.coroutines.delay(1500)
                                            sandboxPaymentState = "SUCCESS"
                                            kotlinx.coroutines.delay(1000)
                                            viewModel.placeOrder(selectedPaymentMethod, checkoutAddress, devLat, devLng) { orderId ->
                                                showPaymentSandbox = false
                                                if (orderId > 0) {
                                                    onNavigate(Screen.Tracking)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("AUTHORIZE WALLET TRANSACTION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "COD" -> {
                                Text(
                                    "Cash On Delivery anti-bot compliance check",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Confirm checkout and promise to pay the rider of $${String.format("%.2f", totalOrderAmt)} cash at: $checkoutAddress",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        sandboxPaymentState = "PROCESSING"
                                        scope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            sandboxPaymentState = "SUCCESS"
                                            kotlinx.coroutines.delay(800)
                                            viewModel.placeOrder(selectedPaymentMethod, checkoutAddress, devLat, devLng) { orderId ->
                                                showPaymentSandbox = false
                                                if (orderId > 0) {
                                                    onNavigate(Screen.Tracking)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("CONFIRM CASH ORDER ON DELIVERY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpiSheet) {
        UpiPaymentSheet(
            amount = totalOrderAmt,
            onSuccess = { txnId ->
                showUpiSheet = false
                viewModel.placeOrder("UPI", checkoutAddress, devLat, devLng) { orderId ->
                    if (orderId > 0) {
                        successAmount = totalOrderAmt
                        successOrderId = orderId.toString()
                        showSuccessOverlay = true
                    }
                }
            },
            onDismiss = { showUpiSheet = false }
        )
    }

    if (showCardSheet) {
        CardPaymentSheet(
            amount = totalOrderAmt,
            onSuccess = { txnId ->
                showCardSheet = false
                viewModel.placeOrder("Credit Card", checkoutAddress, devLat, devLng) { orderId ->
                    if (orderId > 0) {
                        successAmount = totalOrderAmt
                        successOrderId = orderId.toString()
                        showSuccessOverlay = true
                    }
                }
            },
            onDismiss = { showCardSheet = false }
        )
    }

    if (showCodSheet) {
        CodConfirmSheet(
            amount = totalOrderAmt,
            onConfirm = {
                showCodSheet = false
                viewModel.placeOrder("COD", checkoutAddress, devLat, devLng) { orderId ->
                    if (orderId > 0) {
                        successAmount = totalOrderAmt
                        successOrderId = orderId.toString()
                        showSuccessOverlay = true
                    }
                }
            },
            onDismiss = { showCodSheet = false }
        )
    }

    if (showSuccessOverlay) {
        PaymentSuccessOverlay(
            amountPaid = successAmount,
            orderId = successOrderId,
            onDismiss = {
                showSuccessOverlay = false
                onNavigate(Screen.Tracking)
            }
        )
    }
}

fun discountAmount(isGold: Boolean, subtotal: Double): Double {
    return if (isGold) subtotal.coerceAtMost(10.0) else 0.0
}


// --- ORDER TRACKING SIMULATOR ---

@Composable
fun ActiveOrderTrackingView(
    viewModel: PlatformViewModel,
    onNavigate: (Screen) -> Unit
) {
    val activeId by viewModel.activeOrderId.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val activeOrder = orders.find { it.id == activeId }

    if (activeOrder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active order currently tracked.")
                Button(onClick = { onNavigate(Screen.Home) }) {
                    Text("Browse Food")
                }
            }
        }
        return
    }

    var reviewRating by remember { mutableStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewSubmitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Tracker Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order #${activeOrder.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(activeOrder.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(activeOrder.restaurantName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(activeOrder.itemsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Live coordinate map drawing
        item {
            val darkThemeFlow by viewModel.isDarkTheme.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real-Time Delivery Coordination", fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (darkThemeFlow) Color(0xFF1E1E1E) else Color(0xFFE3F2FD))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                val getCache = remember(viewModel) { { id: Int -> viewModel.getCachedRoute(id) } }
                val saveCache = remember(viewModel) { { id: Int, pts: List<Pair<Double, Double>> -> viewModel.cacheRoute(id, pts) } }

                OSMDeliveryMap(
                    orderId = activeOrder.id,
                    customerLat = activeOrder.customerLat,
                    customerLng = activeOrder.customerLng,
                    restaurantLat = activeOrder.restaurantLat,
                    restaurantLng = activeOrder.restaurantLng,
                    driverLat = activeOrder.driverLat,
                    driverLng = activeOrder.driverLng,
                    isDarkTheme = darkThemeFlow,
                    getCachedRoute = getCache,
                    cacheRoute = saveCache,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Continuous Live ETA (Calculated automatically based on coordinates)
        item {
            val startLat = activeOrder.restaurantLat
            val startLng = activeOrder.restaurantLng
            val endLat = activeOrder.customerLat
            val endLng = activeOrder.customerLng
            val driverLat = activeOrder.driverLat
            val driverLng = activeOrder.driverLng
            val status = activeOrder.status

            // Haversine formula for real distance calculation
            val distanceKm = remember(driverLat, driverLng, endLat, endLng) {
                val R = 6371.0 // Radius of the earth in km
                val dLat = Math.toRadians(endLat - driverLat)
                val dLon = Math.toRadians(endLng - driverLng)
                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(driverLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2)
                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                R * c
            }

            val etaText = remember(status, distanceKm) {
                when (status) {
                    "Placed", "Accepted" -> "20 - 25 mins (Restaurant accepting order)"
                    "Preparing", "Cooking" -> "15 - 20 mins (Chef preparing your meal)"
                    "OutForDelivery" -> {
                        val etaMinutes = (distanceKm * 2.5).toInt().coerceAtLeast(1)
                        "Arriving in $etaMinutes mins (~${String.format(java.util.Locale.US, "%.1f", distanceKm)} km away)"
                    }
                    "Delivered" -> "Delivered (Enjoy your meal! 🍽️)"
                    "Cancelled" -> "Cancelled"
                    else -> "Estimating..."
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated Delivery Time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = etaText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = when (status) {
                            "Placed", "Accepted" -> 0.25f
                            "Preparing", "Cooking" -> 0.50f
                            "OutForDelivery" -> 0.75f
                            "Delivered" -> 1.0f
                            else -> 0.0f
                        },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Order Delivered Review Section (With real sentiment detection!)
        if (activeOrder.status == "Delivered") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rate Your BiteCraft Order", fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Simulate Gemini AI review sentiment analyzer which credits $2.00 cashback spts for Positive reviews!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (reviewSubmitted) {
                            Text("Thank you! Your feedback has been categorized as: **${activeOrder.reviewSentiment}**", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("star_rating_bar"),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { index ->
                                    val filled = index <= reviewRating.toInt()
                                    Icon(
                                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "$index Stars",
                                        tint = if (filled) Color(0xFFFFB300) else Color.LightGray,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable { reviewRating = index.toFloat() }
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Rating: ${reviewRating.toInt()} / 5 stars", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFC8019))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                placeholder = { Text("e.g. Absolutely delicious truffle burger, arrived piping hot!") },
                                modifier = Modifier.fillMaxWidth().testTag("review_field"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.submitReview(activeOrder.id, reviewRating, reviewComment)
                                    reviewSubmitted = true
                                },
                                modifier = Modifier.fillMaxWidth().testTag("submit_review_btn")
                            ) {
                                Text("SUBMIT REVIEW")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CLIENT WALLET VIEW (Enterprise Edition) ---

@Composable
fun ClientWalletView(viewModel: PlatformViewModel, onOpenFamily: () -> Unit = {}) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTx.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val totalFamilySpend by viewModel.totalFamilySpend.collectAsState()
    val isUpdating by viewModel.isWalletUpdating.collectAsState()

    var depositAmount by remember { mutableStateOf("10.00") }
    var showUpiDeposit by remember { mutableStateOf(false) }

    // Auto-refresh wallet balance and transactions on entry
    LaunchedEffect(Unit) {
        viewModel.refreshWallet()
    }

    // Quick deposit presets
    val quickAmounts = listOf(100.0, 200.0, 500.0, 1000.0)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Gradient hero wallet card ──────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFF7C3AED))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BiteCraft Wallet", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "₹${String.format("%.2f", balance)}",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Available balance", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Add Money button
                        Button(
                            onClick = { showUpiDeposit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_funds_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Money", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        // Family button
                        OutlinedButton(
                            onClick = onOpenFamily,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Family", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── Quick reload amounts ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Quick Add", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAmounts.forEach { amt ->
                        OutlinedButton(
                            onClick = { viewModel.addFunds(amt) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("₹${amt.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── Custom amount reload ───────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Custom Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = depositAmount,
                            onValueChange = { depositAmount = it },
                            placeholder = { Text("Enter amount") },
                            modifier = Modifier.weight(1f).testTag("wallet_deposit_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            prefix = { Text("₹", fontWeight = FontWeight.Bold) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val amt = depositAmount.toDoubleOrNull() ?: 10.0
                                viewModel.addFunds(amt)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Reload", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ── Family Spending Summary (if members exist) ─────────────────────
        if (familyMembers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF7C3AED).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Family Spending", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF7C3AED))
                            }
                            TextButton(onClick = onOpenFamily) {
                                Text("Manage", color = Color(0xFF7C3AED), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        familyMembers.take(3).forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(member.avatarColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        val pct = if (member.spendingLimit > 0) (member.monthlySpent / member.spendingLimit * 100).toInt() else 0
                                        Text("₹${member.monthlySpent.toInt()} / ₹${member.spendingLimit.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                // Progress
                                Box(modifier = Modifier.width(80.dp)) {
                                    LinearProgressIndicator(
                                        progress = { (member.monthlySpent / member.spendingLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                                        color = Color(member.avatarColor),
                                        trackColor = Color(member.avatarColor).copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF7C3AED).copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Family Spent", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%.2f", totalFamilySpend)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C3AED))
                        }
                    }
                }
            }
        }

        // ── Transaction history title ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transactions", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                if (transactions.isNotEmpty()) {
                    Text("${transactions.size} records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var isAnimated by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            isAnimated = true
                        }
                        val floatOffset by animateFloatAsState(
                            targetValue = if (isAnimated) -10f else 10f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "wallet_card_float"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isAnimated) 1f else 0.8f,
                            animationSpec = tween(600, easing = EaseOutBack),
                            label = "wallet_card_scale"
                        )
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_wallet_card),
                            contentDescription = "Wallet Empty State",
                            modifier = Modifier
                                .size(90.dp)
                                .graphicsLayer {
                                    translationY = floatOffset
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No transactions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            itemsIndexed(transactions) { index, tx ->
                val isCredit = tx.type in listOf("Deposit", "Refund", "Cashback")
                val txIconRes = when (tx.type) {
                    "Deposit" -> com.example.R.drawable.ic_wallet_deposit
                    "Cashback" -> com.example.R.drawable.ic_wallet_cashback
                    "Refund" -> com.example.R.drawable.ic_wallet_refund
                    "FamilyDebit" -> com.example.R.drawable.ic_wallet_family
                    else -> com.example.R.drawable.ic_wallet_shopping
                }

                // Staggered slide/fade animation
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(tx.id) {
                    delay(40L * index)
                    isVisible = true
                }
                val slideOffset by animateDpAsState(
                    targetValue = if (isVisible) 0.dp else 40.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tx_row_slide"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(300, easing = LinearOutSlowInEasing),
                    label = "tx_row_alpha"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .graphicsLayer {
                            translationY = slideOffset.toPx()
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCredit) Color(0xFFDCFCE7) else Color(0xFFFFEEEE)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = txIconRes),
                                contentDescription = tx.type,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = if (tx.memberName.isNotEmpty()) "${tx.type} · ${tx.memberName}" else tx.type,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (isCredit) "+₹${String.format("%.2f", tx.amount)}" else "-₹${String.format("%.2f", tx.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isCredit) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }
    }

    // UPI Deposit Dialog
        if (showUpiDeposit) {
            AlertDialog(
                onDismissRequest = { showUpiDeposit = false },
                title = {
                    Text("Add Money to Wallet", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Enter the amount you would like to deposit to your BiteCraft Wallet.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = depositAmount,
                            onValueChange = { depositAmount = it },
                            placeholder = { Text("Enter amount") },
                            modifier = Modifier.fillMaxWidth().testTag("wallet_dialog_deposit_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            prefix = { Text("₹", fontWeight = FontWeight.Bold) }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = depositAmount.toDoubleOrNull() ?: 10.0
                            viewModel.addFunds(amt)
                            showUpiDeposit = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add Funds", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpiDeposit = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Overlay Loading Spinner during wallet operations
        if (isUpdating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Updating Wallet...",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// --- GEMINI POWERED CUSTOMER SUPPORT CHAT VIEW ---

@Composable
fun GeminiSupportView(viewModel: PlatformViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.supportInputText.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    val prevSize = remember { mutableIntStateOf(messages.size) }
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Auto-scroll and isSending reset when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.size > prevSize.intValue) {
            isSending = false
            prevSize.intValue = messages.size
        }
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickChips = listOf("Track my order", "Request refund", "Change address", "Membership help")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Premium gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF9800), Color(0xFFE64A19))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "BiteCraft AI",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Powered by Gemini",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                TextButton(
                    onClick = { viewModel.clearSupportChat() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Empty state with quick action chips
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🤖", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hey! I'm your BiteCraft AI assistant.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ask me anything about your order, refunds, or membership.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Quick Actions",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // 2x2 chip grid
                    quickChips.chunked(2).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            row.forEach { chip ->
                                SuggestionChip(
                                    onClick = {
                                        if (!isSending) {
                                            viewModel.supportInputText.value = chip
                                            scope.launch {
                                                isSending = true
                                                prevSize.intValue = messages.size
                                                viewModel.sendSupportChat()
                                                delay(8000L)
                                                isSending = false
                                            }
                                        }
                                    },
                                    label = {
                                        Text(
                                            chip,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "User"
                        val timeStr = sdf.format(Date(msg.timestamp))

                        if (isUser) {
                            // User bubble: right-aligned
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = 18.dp,
                                                bottomEnd = 4.dp
                                            )
                                        )
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        text = msg.message,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            // AI bubble: left-aligned with robot avatar
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Robot avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFFFF9800), Color(0xFFE64A19))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🤖", fontSize = 14.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 18.dp,
                                                    topEnd = 18.dp,
                                                    bottomStart = 4.dp,
                                                    bottomEnd = 18.dp
                                                )
                                            )
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg.message,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 38.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI thinking indicator
        AnimatedVisibility(visible = isSending) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BiteCraft AI is thinking...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Input area
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.supportInputText.value = it },
                    placeholder = {
                        Text(
                            "Ask anything about your order...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (!isSending && inputText.isNotBlank()) {
                                scope.launch {
                                    isSending = true
                                    prevSize.intValue = messages.size
                                    viewModel.sendSupportChat()
                                    delay(8000L)
                                    isSending = false
                                }
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (!isSending && inputText.isNotBlank()) {
                            scope.launch {
                                isSending = true
                                prevSize.intValue = messages.size
                                viewModel.sendSupportChat()
                                delay(8000L)
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending && inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isSending && inputText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        .testTag("send_chat_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (!isSending && inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. KITCHEN / RESTAURANT PANEL VIEW
// ==========================================

@Composable
fun KitchenSection(viewModel: PlatformViewModel) {
    val orders by viewModel.orders.collectAsState()
    val kitchenOrders = orders.filter { it.status == "Placed" || it.status == "Accepted" || it.status == "Preparing" }

    var selectedSectionState by remember { mutableStateOf("Pending Orders") } // Pending, Menu management, Performance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Restaurant Portal", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        // Kitchen options scroll menu
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sections = listOf("Pending Orders", "Menu Setup")
            sections.forEach { sect ->
                val active = selectedSectionState == sect
                FilterChip(
                    selected = active,
                    onClick = { selectedSectionState = sect },
                    label = { Text(sect, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedSectionState) {
            "Pending Orders" -> {
                if (kitchenOrders.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active pending orders currently mapped in kitchen.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(kitchenOrders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(order.status.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(order.itemsSummary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Client Details: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (order.status == "Placed") {
                                            Button(
                                                onClick = { viewModel.updateOrderStatusByKitchen(order.id, "Accepted") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("ACCEPT ORDER")
                                            }
                                        } else if (order.status == "Accepted") {
                                            Button(
                                                onClick = { viewModel.updateOrderStatusByKitchen(order.id, "Preparing") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("START COOKING")
                                            }
                                        } else if (order.status == "Preparing") {
                                            Button(
                                                onClick = { viewModel.updateOrderStatusByKitchen(order.id, "OutForDelivery") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("HANDOFF TO DRIVER")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Menu Setup" -> {
                // Dynamic Menu Setup Add/Edit mock spts
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Onboard New Gourmet Dish", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf("Dish name", "Price ($)", "Category", "Select Addons comma options").forEach { placeholder ->
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text(placeholder) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                            Text("Onboard & Sync Dish")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DELIVERY PARTNER APPLICATION VIEW
// ==========================================

@Composable
fun RiderSection(viewModel: PlatformViewModel) {
    val orders by viewModel.orders.collectAsState()
    val outForDeliveryOrders = orders.filter { it.status == "OutForDelivery" }

    var availabilityState by remember { mutableStateOf(true) } // On duty, Off duty

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delivery Rider Portal", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (availabilityState) "On Duty" else "Off Duty", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Switch(checked = availabilityState, onCheckedChange = { availabilityState = it })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SOS Button
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Emergency, contentDescription = "SOS", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SOS EMERGENCY ALERTS", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!availabilityState) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Go 'On Duty' to receive gourmet dispatch notifications!")
            }
        } else {
            if (outForDeliveryOrders.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No dispatch orders waiting for rider coordination.")
                }
            } else {
                Text("Assigned Transit Orders", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(outForDeliveryOrders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Delivery Job #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Pickup: ${order.restaurantName}", fontWeight = FontWeight.Medium)
                                Text("Dropoff Client: ${order.deliveryAddress}", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.updateOrderStatusByKitchen(order.id, "Delivered") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("MARK JOB AS COMPLETED")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SUPER ADMIN PANEL VIEW
// ==========================================

@Composable
fun AdminSection(viewModel: PlatformViewModel) {
    val orders by viewModel.orders.collectAsState()
    
    // KPI parameters calculation
    val salesIncome = orders.sumOf { it.totalAmount }
    val commissionAmount = salesIncome * 0.15

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Global Admin Panel", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = { viewModel.wipeDatabase() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("RE-CALIBRATE PLATFORM")
                }
            }
        }

        // KPI metric blocks
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Gross Sales", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", salesIncome)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("15% Commissions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", commissionAmount)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4CAF50))
                    }
                }
            }
        }

        // Fraud Warning Auditing Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFFFFB300))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Security Alert", tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Audit & Fraud Signal Tracker", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Text("Platform auditing matches no latency anomalies or duplicative orders.", fontSize = 11.sp, color = Color(0xFFCC5200))
                    }
                }
            }
        }

        // Static sales graph simulation using classic Canvas bars
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2026 Hourly Demand Forecast (Simulation)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barColor = Color(0xFFFF5722)
                            val heights = listOf(0.3f, 0.5f, 0.2f, 0.8f, 0.9f, 0.4f, 0.7f, 0.6f)
                            val pad = w / (heights.size * 2)
                            val barW = w / heights.size - pad

                            heights.forEachIndexed { idx, rat ->
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(idx * (barW + pad), h - h * rat),
                                    size = androidx.compose.ui.geometry.Size(barW, h * rat),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("9 AM", fontSize = 9.sp)
                        Text("12 PM (Peak)", fontSize = 9.sp)
                        Text("3 PM", fontSize = 9.sp)
                        Text("7 PM (Peak)", fontSize = 9.sp)
                    }
                }
            }
        }

        // Log history audits
        item {
            Text("Active System Audit Trail", fontWeight = FontWeight.Bold)
        }

        items(orders) { order ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AUDIT: Code #O-${order.id} verified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Source: ${order.restaurantName} → ${order.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (order.status == "Delivered") Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (order.status == "Delivered") Color(0xFF2E7D32) else Color(0xFF546E7A),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// --- SAVED ADDRESS MANAGEMENT DIALOGS ---

@Composable
fun AddressLabelDialog(
    address: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var labelInput by remember { mutableStateOf("Home") }
    val presets = listOf("Home", "Work", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Label this address", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = address,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Preset chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = labelInput == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { labelInput = preset },
                            label = { Text(preset) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Custom text input
                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it },
                    label = { Text("Address Label") },
                    placeholder = { Text("e.g. Gym, Friend's house") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("address_label_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(labelInput.trim().ifEmpty { "Address" }) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Address")
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

@Composable
fun AddressManagementDialog(
    viewModel: PlatformViewModel,
    onDismiss: () -> Unit,
    onAddNewAddress: () -> Unit
) {
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val activeAddress by viewModel.userAddress.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Delivery Address", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (savedAddresses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No saved addresses yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(savedAddresses) { addr ->
                            val isDefault = addr.isDefault
                            val isActive = addr.fullAddress == activeAddress
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.userAddress.value = addr.fullAddress
                                        viewModel.updateDeviceLocation(addr.latitude, addr.longitude)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) 
                                                     else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when(addr.label.lowercase()) {
                                            "home" -> Icons.Filled.Home
                                            "work" -> Icons.Filled.Work
                                            else -> Icons.Filled.Place
                                        },
                                        contentDescription = null,
                                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = addr.label,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isDefault) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("DEFAULT", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(
                                            text = addr.fullAddress,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    // Make Default button (star/favorite icon)
                                    IconButton(
                                        onClick = { viewModel.makeAddressDefault(addr.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Set Default",
                                            tint = if (isDefault) Color(0xFFFFD54F) else Color.Gray.copy(alpha = 0.4f)
                                        )
                                    }
                                    
                                    // Delete button
                                    IconButton(
                                        onClick = { viewModel.deleteAddress(addr.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Address",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onAddNewAddress,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Address")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
