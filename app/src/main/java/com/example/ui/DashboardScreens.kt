package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import android.webkit.WebView
import kotlinx.coroutines.launch
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

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
fun CustomerSection(
    viewModel: PlatformViewModel,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val items = listOf(
                    Triple(Screen.Home, Icons.Outlined.Home, "Explore"),
                    Triple(Screen.Cart, Icons.Outlined.ShoppingCart, "Cart"),
                    Triple(Screen.Wallet, Icons.Outlined.AccountBalanceWallet, "Wallet"),
                    Triple(Screen.Support, Icons.Outlined.Sms, "AI Chat")
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
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                is Screen.Home -> ExploreView(viewModel, onNavigate)
                is Screen.Detail -> RestaurantDetailView(viewModel, onNavigate)
                is Screen.Cart -> ClientCartView(viewModel, onNavigate)
                is Screen.Tracking -> ActiveOrderTrackingView(viewModel, onNavigate)
                is Screen.Wallet -> ClientWalletView(viewModel)
                is Screen.Support -> GeminiSupportView(viewModel)
                else -> ExploreView(viewModel, onNavigate)
            }
        }
    }
}

@Composable
fun ExploreView(
    viewModel: PlatformViewModel,
    onNavigate: (Screen) -> Unit
) {
    val search by viewModel.searchQuery.collectAsState()
    val category by viewModel.activeCategory.collectAsState()
    val restaurantsList by viewModel.filteredRestaurants.collectAsState()
    val isGold by viewModel.isGoldMember.collectAsState()
    
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
                // Address locator in professional Swiggy header format
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Current Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Home Block 4",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Caret",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = viewModel.userAddress.collectAsState().value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGold) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            .clickable { viewModel.toggleGoldMembership() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGold) {
                            Text("👑", fontSize = 18.sp)
                        } else {
                            Text(
                                text = "TV", // Tharun Velamakuru
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("UP TO 50% OFF", fontSize = 9.sp, color = Color(0xFFFC8019), fontWeight = FontWeight.Black)
                                Text("🍔", fontSize = 26.sp)
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("FRESH ITEMS", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)
                                Text("🥦", fontSize = 26.sp)
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("FLAT 30% OFF", fontSize = 9.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Black)
                                Text("🍷", fontSize = 26.sp)
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("SEND PARCEL", fontSize = 9.sp, color = Color(0xFF8E24AA), fontWeight = FontWeight.Black)
                                Text("🎁", fontSize = 26.sp)
                            }
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
                
                val premiumCategories = listOf(
                    Triple("Burgers", "🍔", CategoryBurger),
                    Triple("Pizza", "🍕", CategoryPizza),
                    Triple("Sushi", "🍣", CategorySushi),
                    Triple("Healthy", "🥗", CategoryHealthy),
                    Triple("Desserts", "🍰", CategoryDessert),
                    Triple("Ramen", "🍜", CategoryBurger)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(premiumCategories) { (title, emoji, color) ->
                        val isSelected = category == title
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
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 32.sp)
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

        if (restaurantsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Storefront, contentDescription = "Empty", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching kitchens found. Modify filters!", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // Back toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.Home) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

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
                            imageVector = Icons.Default.ArrowForward,
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

    var checkoutAddress by remember { mutableStateOf("Suite 301, Sector 12, Innovation Block, Cloud City") }
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
                    isLoadingLocation = false
                    viewModel.updateDeviceLocation(lat, lng)
                    checkoutAddress = "Real-Time GPS Lat: ${String.format("%.5f", lat)}, Lng: ${String.format("%.5f", lng)}"
                    Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
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
                    val devLat by viewModel.deviceLatitude.collectAsState()
                    val devLng by viewModel.deviceLongitude.collectAsState()
                    val subtotal = cart.sumOf { it.price * it.quantity }
                    val delivery = if (isGold && subtotal > 12.0) 0.0 else 2.50
                    val totalOrderAmt = subtotal + delivery - discountAmount(isGold, subtotal)

                    Button(
                        onClick = {
                            if (selectedPaymentMethod == "Wallet" && walletBal < totalOrderAmt) {
                                Toast.makeText(context, "Insufficient balance! Please load money in BiteCraft Wallet first.", Toast.LENGTH_LONG).show()
                            } else {
                                sandboxPaymentState = "IDLE"
                                sandboxOtpInput = ""
                                upiPinInput = ""
                                isPushNotificationVisible = false
                                showPaymentSandbox = true
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
                    val subtotal = cart.sumOf { it.price * it.quantity }
                    val delivery = if (isGold && subtotal > 12.0) 0.0 else 2.50
                    val totalOrderAmt = subtotal + delivery - discountAmount(isGold, subtotal)
                    val devLat by viewModel.deviceLatitude.collectAsState()
                    val devLng by viewModel.deviceLongitude.collectAsState()

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
                                                    "upi://pay?pa=bitecraft@okhdfc&pn=BiteCraft%20Foods&am=${String.format(java.util.Locale.US, "%.2f", totalOrderAmt)}&cu=INR&tn=BiteCraft%20Order"
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
}

fun discountAmount(isGold: Boolean, subtotal: Double): Double {
    return if (isGold) subtotal.coerceAtMost(10.0) else 0.0
}

@Composable
fun InteractiveLeafletMap(
    customerLat: Double,
    customerLng: Double,
    restaurantLat: Double,
    restaurantLng: Double,
    driverLat: Double,
    driverLng: Double,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Format coordinates using Locale.US to prevent European/localized comma syntax errors in HTML/JS (such as 12,9 instead of 12.9)
    val rLatStr = remember(restaurantLat) { String.format(java.util.Locale.US, "%.7f", restaurantLat) }
    val rLngStr = remember(restaurantLng) { String.format(java.util.Locale.US, "%.7f", restaurantLng) }
    val cLatStr = remember(customerLat) { String.format(java.util.Locale.US, "%.7f", customerLat) }
    val cLngStr = remember(customerLng) { String.format(java.util.Locale.US, "%.7f", customerLng) }
    val dLatStr = remember(driverLat) { String.format(java.util.Locale.US, "%.7f", driverLat) }
    val dLngStr = remember(driverLng) { String.format(java.util.Locale.US, "%.7f", driverLng) }

    val rawHtml = remember(rLatStr, rLngStr, cLatStr, cLngStr, isDarkTheme) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map { margin: 0; padding: 0; height: 100%; width: 100%; background: ${if (isDarkTheme) "#121212" else "#f0f0f0"}; }
                .pulse-marker {
                    background: #1e88e5;
                    border-radius: 50%;
                    box-shadow: 0 0 0 0 rgba(30, 136, 229, 0.7);
                    animation: pulse-animation 1.5s infinite;
                }
                @keyframes pulse-animation {
                    0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(30, 136, 229, 0.7); }
                    70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(30, 136, 229, 0); }
                    100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(30, 136, 229, 0); }
                }
                .custom-div-icon {
                    background: transparent;
                    border: none;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([$rLatStr, $rLngStr], 14);
                var tileUrl = '${if (isDarkTheme) "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" else "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"}';
                L.tileLayer(tileUrl, {
                    attribution: '&copy; OpenStreetMap'
                }).addTo(map);

                var restaurantIcon = L.divIcon({
                    html: '<div style="font-size: 26px; filter: drop-shadow(0px 2px 4px rgba(0,0,0,0.3));">🏪</div>',
                    iconSize: [30, 30],
                    className: 'custom-div-icon'
                });

                var customerIcon = L.divIcon({
                    html: '<div class="pulse-marker" style="width: 14px; height: 14px; border: 2px solid white; display: flex; align-items: center; justify-content: center; border-radius: 50%;"><div style="width: 6px; height: 6px; border-radius: 50%; background: #1e88e5;"></div></div>',
                    iconSize: [20, 20],
                    className: 'custom-div-icon'
                });

                var driverIcon = L.divIcon({
                    html: '<div style="font-size: 32px; filter: drop-shadow(0px 3px 6px rgba(0,0,0,0.45));">🏍️</div>',
                    iconSize: [38, 38],
                    className: 'custom-div-icon'
                });

                var rMarker = L.marker([$rLatStr, $rLngStr], { icon: restaurantIcon }).addTo(map);
                var cMarker = L.marker([$cLatStr, $cLngStr], { icon: customerIcon }).addTo(map);
                var dMarker = L.marker([$rLatStr, $rLngStr], { icon: driverIcon }).addTo(map);

                var routePoly = L.polyline([
                    [$rLatStr, $rLngStr],
                    [$rLatStr, $rLngStr],
                    [$cLatStr, $cLngStr]
                ], { color: '#FC8019', weight: 5, opacity: 0.9, dashArray: '6, 8' }).addTo(map);

                var group = new L.featureGroup([rMarker, cMarker, dMarker]);
                map.fitBounds(group.getBounds().pad(0.2));

                function updateDriver(lat, lng) {
                    dMarker.setLatLng([lat, lng]);
                    routePoly.setLatLngs([
                        [$rLatStr, $rLngStr],
                        [lat, lng],
                        [$cLatStr, $cLngStr]
                    ]);
                    var updatedGroup = new L.featureGroup([rMarker, cMarker, dMarker]);
                    map.fitBounds(updatedGroup.getBounds().pad(0.15));
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                loadDataWithBaseURL("https://unpkg.com", rawHtml, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.evaluateJavascript("if (typeof updateDriver === 'function') { updateDriver($dLatStr, $dLngStr); }") { }
        }
    )
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

    // AI Predictive inputs
    var weatherInput by remember { mutableStateOf("Clear Sky") }
    var trafficInput by remember { mutableStateOf("Low Traffic") }
    val predictiveTimeResult by viewModel.predictiveTimeResult.collectAsState()
    val isPredictiveLoading by viewModel.isPredictiveLoading.collectAsState()

    var reviewRating by remember { mutableStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewSubmitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            var selectedMapTab by remember { mutableStateOf("gps") } // gps or radar
            val darkThemeFlow by viewModel.isDarkTheme.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real-Time Delivery Coordination", fontWeight = FontWeight.Bold)
                
                // Segmented Toggle control
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedMapTab == "gps") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedMapTab = "gps" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "LIVE MAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMapTab == "gps") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedMapTab == "radar") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedMapTab = "radar" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "RADAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMapTab == "radar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                if (selectedMapTab == "gps") {
                    InteractiveLeafletMap(
                        customerLat = activeOrder.customerLat,
                        customerLng = activeOrder.customerLng,
                        restaurantLat = activeOrder.restaurantLat,
                        restaurantLng = activeOrder.restaurantLng,
                        driverLat = activeOrder.driverLat,
                        driverLng = activeOrder.driverLng,
                        isDarkTheme = darkThemeFlow,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Drawing delivery path coordinate offsets inside canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw simple grid map roads
                        val strokeGridColor = if (darkThemeFlow) Color.White.copy(alpha = 0.05f) else Color.Blue.copy(alpha = 0.05f)
                        for (x in 20..(width.toInt()) step 100) {
                            drawLine(strokeGridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), height), strokeWidth = 2f)
                        }
                        for (y in 20..(height.toInt()) step 100) {
                            drawLine(strokeGridColor, Offset(0f, y.toFloat()), Offset(width, y.toFloat()), strokeWidth = 2f)
                        }

                        // Restaurant coordinates
                        val rX = width * 0.2f
                        val rY = height * 0.2f
                        // Customer coordinates
                        val cX = width * 0.8f
                        val cY = height * 0.8f

                        // Draw route line
                        drawLine(
                            color = if (darkThemeFlow) Color.DarkGray else Color.LightGray,
                            start = Offset(rX, rY),
                            end = Offset(cX, cY),
                            strokeWidth = 4f
                        )

                        // Driver position loaded from live DB Coordinate updates
                        val baseRestLat = activeOrder.restaurantLat
                        val baseRestLng = activeOrder.restaurantLng
                        val baseCustLat = activeOrder.customerLat
                        val baseCustLng = activeOrder.customerLng

                        val latDelta = baseCustLat - baseRestLat
                        val lngDelta = baseCustLng - baseRestLng

                        val activeLatOffset = if (Math.abs(latDelta) > 0.00001) {
                            (activeOrder.driverLat - baseRestLat) / latDelta
                        } else 0.5

                        val activeLngOffset = if (Math.abs(lngDelta) > 0.00001) {
                            (activeOrder.driverLng - baseRestLng) / lngDelta
                        } else 0.5

                        val dX = (rX + (cX - rX) * activeLngOffset).toFloat().coerceIn(rX, cX)
                        val dY = (rY + (cY - rY) * activeLatOffset).toFloat().coerceIn(rY, cY)

                        // Draw Restaurant
                        drawCircle(Color(0xFFFF9800), radius = 12f, center = Offset(rX, rY))
                        // Draw Customer
                        drawCircle(Color(0xFF1E88E5), radius = 12f, center = Offset(cX, cY))
                        // Draw Rider
                        drawCircle(Color(0xFF4CAF50), radius = 15f, center = Offset(dX, dY))
                        drawCircle(Color.White, radius = 7f, center = Offset(dX, dY))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("🏪 RESTAURANT", color = Color(0xFFFF9800), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("🏍️ RIDER (LIVE)", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("📍 YOU", color = Color(0xFF1E88E5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live predictive time (Using Gemini AI)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("AI Predictive ETA Calculator", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weatherInput,
                            onValueChange = { weatherInput = it },
                            placeholder = { Text("Weather") },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = trafficInput,
                            onValueChange = { trafficInput = it },
                            placeholder = { Text("Traffic") },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runAiPredictiveTime(weatherInput, trafficInput, 3.2f) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_predict_btn")
                    ) {
                        if (isPredictiveLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("PREDICT FAST ETA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (predictiveTimeResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(predictiveTimeResult, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
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

// --- CLIENT WALLET VIEW ---

@Composable
fun ClientWalletView(viewModel: PlatformViewModel) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTx.collectAsState()

    var depositAmount by remember { mutableStateOf("10.00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text("BiteCraft Wallet", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Card displaying balance
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Available Wallet Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$${String.format("%.2f", balance)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Instant deposit, automated order debits, & review cashbacks.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
            }
        }

        // Add funds input block
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reload Wallet Funds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = depositAmount,
                            onValueChange = { depositAmount = it },
                            placeholder = { Text("e.g. 20.00") },
                            modifier = Modifier.weight(1f).testTag("wallet_deposit_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val amt = depositAmount.toDoubleOrNull() ?: 10.0
                                viewModel.addFunds(amt)
                            },
                            modifier = Modifier.testTag("add_funds_btn")
                        ) {
                            Text("Reload")
                        }
                    }
                }
            }
        }

        // Transaction Title
        item {
            Text("Recent Transactions History", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Text("No transactions logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(transactions) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val icon = if (tx.type == "Debit") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                icon,
                                contentDescription = tx.type,
                                tint = if (tx.type == "Debit") Color.Red else Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(tx.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = if (tx.type == "Debit") "-$${String.format("%.2f", tx.amount)}" else "+$${String.format("%.2f", tx.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.type == "Debit") Color.Red else Color.Green
                    )
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
            Column {
                Text("BiteCraft AI Support", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Generative chatbot on calls & refunds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { viewModel.clearSupportChat() }) {
                Text("Clear Chat", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Message List
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp)) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type any message to initiate help desk queries.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(12.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.supportInputText.value = it },
                placeholder = { Text("e.g. Can I request a refund, change address?") },
                modifier = Modifier.weight(1f).testTag("chat_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendSupportChat() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("send_chat_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White)
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
