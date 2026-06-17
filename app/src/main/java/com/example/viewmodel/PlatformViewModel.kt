package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.CartItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DishEntity
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.FamilyTransactionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.RestaurantEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.repository.AppRepository
import com.example.data.repository.SplitBillEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.local.SavedAddressEntity
import com.example.data.local.UserEntity

// Payment state enum
enum class PaymentState { IDLE, PROCESSING, OTP_PENDING, SUCCESS, FAILURE }

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)

    // Roles: "Customer", "Kitchen", "Rider", "Admin"
    val currentRole = MutableStateFlow("Customer")

    // Navigation and sub-state parameters
    val selectedRestaurantId = MutableStateFlow<Int?>(null)
    val searchQuery = MutableStateFlow("")
    val activeCategory = MutableStateFlow("All")

    val userAddress = MutableStateFlow("Tap to set delivery location")
    val isDarkTheme = MutableStateFlow(false)

    val categories: StateFlow<List<com.example.data.local.CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Auth & User Identity (Directly from Database, no cache) ---
    val currentUserFlow: kotlinx.coroutines.flow.Flow<UserEntity?> = repository.dao.observeCurrentUser()

    val isLoggedIn: StateFlow<Boolean> = currentUserFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isGoldMember: StateFlow<Boolean> = currentUserFlow
        .map { it?.isGoldMember ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserPhone: StateFlow<String> = currentUserFlow
        .map { it?.phone ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val currentUserName: StateFlow<String> = currentUserFlow
        .map { it?.name ?: "Foodie" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Foodie")

    val currentUserEmail: StateFlow<String> = currentUserFlow
        .map { it?.email ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val currentUserId: StateFlow<Int> = currentUserFlow
        .map { it?.id ?: -1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val savedAddresses: StateFlow<List<SavedAddressEntity>> = currentUserId
        .flatMapLatest { uid ->
            if (uid >= 0) repository.dao.getSavedAddresses(uid)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    val deviceLatitude = MutableStateFlow<Double?>(null)
    val deviceLongitude = MutableStateFlow<Double?>(null)

    // --- Navigation flags for new screens ---
    val showFamilyScreen = MutableStateFlow(false)
    val showSettingsScreen = MutableStateFlow(false)

    fun updateDeviceLocation(lat: Double, lng: Double) {
        deviceLatitude.value = lat
        deviceLongitude.value = lng
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    // Customize dialog parameters
    val selectedSpiceLevel = MutableStateFlow("Medium")
    val selectedAddons = MutableStateFlow<Set<String>>(emptySet())
    val customNotes = MutableStateFlow("")

    // AI dynamic attributes
    val aiRecommendationResult = MutableStateFlow("")
    val isAiLoading = MutableStateFlow(false)
    val predictiveTimeResult = MutableStateFlow("")
    val isPredictiveLoading = MutableStateFlow(false)

    // Active order being tracked
    val activeOrderId = MutableStateFlow<Int?>(null)
    val supportInputText = MutableStateFlow("")

    // Cache for OSRM route points to avoid redundant network queries when switching tabs
    private val _routePointsCache = mutableMapOf<Int, List<Pair<Double, Double>>>()

    fun getCachedRoute(orderId: Int): List<Pair<Double, Double>>? {
        return _routePointsCache[orderId]
    }

    fun cacheRoute(orderId: Int, points: List<Pair<Double, Double>>) {
        _routePointsCache[orderId] = points
    }

    // --- Payment State ---
    val paymentState = MutableStateFlow(PaymentState.IDLE)
    val paymentError = MutableStateFlow("")
    val lastPaymentMethod = MutableStateFlow("UPI")

    // Split Bill state
    val selectedSplitMembers = MutableStateFlow<Set<Int>>(emptySet())
    val useEqualSplit = MutableStateFlow(true)
    val customSplitAmounts = MutableStateFlow<Map<Int, Double>>(emptyMap())

    // Observe DB records continuously using combine for listings
    val rawRestaurants: StateFlow<List<RestaurantEntity>> = repository.restaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDishes: StateFlow<List<DishEntity>> = repository.dao.getAllDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRestaurants = combine(rawRestaurants, allDishes, searchQuery, activeCategory) { rests, dishes, query, cat ->
        rests.filter { rest ->
            val matchesQuery = if (query.isBlank()) true else {
                rest.name.contains(query, ignoreCase = true) ||
                rest.cuisine.contains(query, ignoreCase = true) ||
                dishes.any { it.restaurantId == rest.id && it.name.contains(query, ignoreCase = true) }
            }
            val matchesCategory = if (cat == "All") true
                                  else if (cat == "Veg Only") rest.isVeg
                                  else if (cat == "Promoted") rest.isPromoted
                                  else rest.cuisine.contains(cat, ignoreCase = true) || rest.name.contains(cat, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTx: StateFlow<List<WalletTransactionEntity>> = repository.walletTx
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- New: Family & Payment Method flows ---
    val familyMembers: StateFlow<List<FamilyMemberEntity>> = repository.familyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyTransactions: StateFlow<List<FamilyTransactionEntity>> = repository.familyTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.paymentMethods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet balance (Observe the UserEntity in Room directly to always stay synchronized with the remote balance)
    val walletBalance: StateFlow<Double> = repository.dao.observeCurrentUser()
        .map { user -> user?.walletBalance ?: 100.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100.0)

    // Total family spending
    val totalFamilySpend = familyMembers.map { members ->
        members.sumOf { it.monthlySpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        startCustomerActiveOrderSync()
        viewModelScope.launch {
            // Load default address locally
            currentUserFlow.collect { user ->
                if (user != null) {
                    repository.dao.getDefaultAddress(user.id)?.let {
                        userAddress.value = it.fullAddress
                    }
                }
            }
        }

        viewModelScope.launch {
            // Restore active order locally on startup
            try {
                val activeOrder = repository.orders.first().find {
                    it.status == "Placed" || it.status == "Accepted" || it.status == "Preparing" || it.status == "OutForDelivery"
                }
                if (activeOrder != null) {
                    activeOrderId.value = activeOrder.id
                }
            } catch (e: Exception) {
                // Ignore startup recovery errors
            }

            // Sync profile & address from remote in the background
            try {
                val localUser = repository.dao.getCurrentUser()
                if (localUser != null) {
                    val syncedUser = repository.loginOrCreateUser(localUser.phone, localUser.name, role = currentRole.value)
                    repository.dao.getDefaultAddress(syncedUser.id)?.let {
                        userAddress.value = it.fullAddress
                    }
                }
            } catch (e: Exception) {
                // Ignore background sync errors when offline
            }
        }
    }

    // On-the-fly fetching for specific restaurants and dishes
    fun getDishesForSelectedRestaurant(): StateFlow<List<DishEntity>> {
        val restId = selectedRestaurantId.value ?: -1
        return repository.dao.getDishesForRestaurant(restId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Role switcher
    fun switchRole(role: String) {
        currentRole.value = role
    }

    // Cart integrations
    fun addToCart(dish: DishEntity, qty: Int) {
        viewModelScope.launch {
            val addonsList = selectedAddons.value.joinToString(", ")
            repository.addCartItem(dish, qty, selectedSpiceLevel.value, addonsList, customNotes.value)
            // Reset modal states
            selectedSpiceLevel.value = "Medium"
            selectedAddons.value = emptySet()
            customNotes.value = ""
        }
    }

    fun modifyCartQuantity(itemId: Int, currentQty: Int, increment: Boolean) {
        viewModelScope.launch {
            if (increment) {
                repository.dao.updateCartQuantity(itemId, currentQty + 1)
            } else {
                if (currentQty <= 1) {
                    repository.dao.deleteCartItem(itemId)
                } else {
                    repository.dao.updateCartQuantity(itemId, currentQty - 1)
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.dao.clearCart()
        }
    }

    val isWalletUpdating = MutableStateFlow(false)

    // Wallet funding
    fun addFunds(amount: Double) {
        viewModelScope.launch {
            isWalletUpdating.value = true
            try {
                repository.addWalletFunds(amount)
            } finally {
                isWalletUpdating.value = false
            }
        }
    }

    // Refresh wallet balance and transactions from remote Supabase DB
    fun refreshWallet() {
        viewModelScope.launch {
            isWalletUpdating.value = true
            try {
                repository.getWalletBalance()
            } finally {
                isWalletUpdating.value = false
            }
        }
    }

    // Membership toggle
    fun toggleGoldMembership() {
        viewModelScope.launch {
            val user = repository.dao.getCurrentUser()
            if (user != null) {
                val updated = user.copy(isGoldMember = !user.isGoldMember)
                repository.dao.insertUser(updated)
            }
        }
    }

    // Checkout with enhanced payment support
    fun placeOrder(
        paymentMethod: String,
        address: String,
        userLat: Double? = null,
        userLng: Double? = null,
        splitEntries: List<SplitBillEntry> = emptyList(),
        callback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val promo = if (isGoldMember.value) "GOLD100" else ""
            val orderId = repository.checkout(paymentMethod, address, promo, userLat, userLng, splitEntries)
            if (orderId > 0) {
                activeOrderId.value = orderId
            }
            callback(orderId)
        }
    }

    // UPI Payment simulation
    fun processUpiPayment(upiId: String, amount: Double, otp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            paymentState.value = PaymentState.PROCESSING
            val success = repository.simulateUpiPayment(upiId, amount, otp)
            if (success) {
                paymentState.value = PaymentState.SUCCESS
            } else {
                paymentState.value = PaymentState.FAILURE
                paymentError.value = "Invalid OTP. Please try again."
            }
            onResult(success)
        }
    }

    // Card payment simulation
    fun processCardPayment(cardLast4: String, amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            paymentState.value = PaymentState.PROCESSING
            val success = repository.simulateCardPayment(cardLast4, amount)
            if (success) {
                paymentState.value = PaymentState.SUCCESS
            } else {
                paymentState.value = PaymentState.FAILURE
                paymentError.value = "Card declined. Please check your details."
            }
            onResult(success)
        }
    }

    fun resetPaymentState() {
        paymentState.value = PaymentState.IDLE
        paymentError.value = ""
    }

    // AI Smart Voice / Recommendations
    fun runAiMoodRecommendation(promptText: String) {
        viewModelScope.launch {
            isAiLoading.value = true
            val fullPrompt = "Recommend delicious combination items from BiteCraft based on this mood preference: \"$promptText\". Use bold markdown, make it feel super premium."
            val reply = GeminiClient.generateContent(fullPrompt)
            aiRecommendationResult.value = reply
            isAiLoading.value = false
        }
    }

    // Predictive Delivery times
    fun runAiPredictiveTime(weather: String, traffic: String, distanceKm: Float) {
        viewModelScope.launch {
            isPredictiveLoading.value = true
            val prompt = "Predict precise food delivery times for cloud kitchen in traffic condition: '$traffic', weather: '$weather', distance: '$distanceKm' km. Return a highly professional, short paragraph with bold times."
            val elapsed = GeminiClient.generateContent(prompt)
            predictiveTimeResult.value = elapsed
            isPredictiveLoading.value = false
        }
    }

    // Submit Order rating and review (with automated Gemini Sentiment Analysis!)
    fun submitReview(orderId: Int, rating: Float, comment: String) {
        viewModelScope.launch {
            repository.addPromoTextReview(orderId, rating, comment)
        }
    }

    // Support Chat
    fun sendSupportChat() {
        val text = supportInputText.value.trim()
        if (text.isEmpty()) return
        supportInputText.value = ""
        viewModelScope.launch {
            repository.insertChatMessage("User", text)
        }
    }

    fun clearSupportChat() {
        viewModelScope.launch {
            repository.dao.clearChatMessages()
        }
    }

    // Kitchen Actions
    fun updateOrderStatusByKitchen(orderId: Int, step: String) {
        viewModelScope.launch {
            repository.dao.updateOrderStatus(orderId, step)
        }
    }

    // Remote Connected Actions
    private var kitchenSyncJob: kotlinx.coroutines.Job? = null
    fun startKitchenSyncing() {
        if (kitchenSyncJob != null) return
        kitchenSyncJob = viewModelScope.launch {
            while (true) {
                repository.fetchRemoteOrders()
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    private var riderSyncJob: kotlinx.coroutines.Job? = null
    fun startRiderSyncing() {
        if (riderSyncJob != null) return
        riderSyncJob = viewModelScope.launch {
            while (true) {
                repository.fetchRemoteOrders()
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun updateOrderStatusRemote(orderId: String, localId: Int, status: String, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            repository.updateOrderRemoteStatus(orderId, localId, status, lat, lng)
        }
    }

    fun acceptRiderJob(orderId: String, localId: Int, driverName: String, driverPhone: String, driverLat: Double, driverLng: Double) {
        viewModelScope.launch {
            repository.acceptRiderOnRemote(orderId, localId, driverName, driverPhone, driverLat, driverLng)
        }
    }

    private val activeLocationSimulationJobs = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    fun startRiderLocationSimulation(orderId: String, localId: Int, restLat: Double, restLng: Double, custLat: Double, custLng: Double) {
        if (activeLocationSimulationJobs.containsKey(orderId)) return
        val job = viewModelScope.launch {
            var currentLat = restLat
            var currentLng = restLng
            val steps = 15
            for (i in 1..steps) {
                kotlinx.coroutines.delay(3000)
                val faction = i.toDouble() / steps.toDouble()
                currentLat = restLat + (custLat - restLat) * faction
                currentLng = restLng + (custLng - restLng) * faction
                
                val status = if (i == steps) "Delivered" else "OutForDelivery"
                repository.updateOrderRemoteStatus(orderId, localId, status, currentLat, currentLng)
                
                if (status == "Delivered") {
                    break
                }
            }
            activeLocationSimulationJobs.remove(orderId)
        }
        activeLocationSimulationJobs[orderId] = job
    }

    // --- Family Management ---
    fun addFamilyMember(name: String, email: String, spendingLimit: Double, avatarColor: Long) {
        viewModelScope.launch {
            repository.addFamilyMember(name, email, spendingLimit, avatarColor)
        }
    }

    fun removeFamilyMember(id: Int) {
        viewModelScope.launch {
            repository.removeFamilyMember(id)
        }
    }

    fun updateMemberSpendingLimit(id: Int, limit: Double) {
        viewModelScope.launch {
            repository.updateMemberSpendingLimit(id, limit)
        }
    }

    fun toggleSplitMember(memberId: Int) {
        val current = selectedSplitMembers.value.toMutableSet()
        if (current.contains(memberId)) current.remove(memberId) else current.add(memberId)
        selectedSplitMembers.value = current
    }

    fun computeSplitAmounts(totalAmount: Double): Map<Int, Double> {
        val memberIds = selectedSplitMembers.value.toList()
        if (memberIds.isEmpty()) return emptyMap()
        return if (useEqualSplit.value) {
            val perPerson = totalAmount / memberIds.size
            memberIds.associateWith { perPerson }
        } else {
            customSplitAmounts.value
        }
    }

    // --- Payment Methods ---
    fun savePaymentMethod(type: String, label: String, maskedValue: String) {
        viewModelScope.launch {
            repository.addPaymentMethod(type, label, maskedValue)
        }
    }

    fun deletePaymentMethod(id: Int) {
        viewModelScope.launch {
            repository.removePaymentMethod(id)
        }
    }

    fun setDefaultPayment(id: Int) {
        viewModelScope.launch {
            repository.setDefaultPaymentMethod(id)
        }
    }

    // Super Admin Reset / Recalibrate Database
    fun wipeDatabase() {
        viewModelScope.launch {
            repository.dao.clearRestaurants()
            repository.dao.clearCart()
            repository.dao.clearChatMessages()
            // Force re-seed
            switchRole("Customer")
            selectedRestaurantId.value = null
            activeOrderId.value = null
        }
    }

    // --- Auth Functions ---
    fun loginUser(phone: String, name: String) {
        viewModelScope.launch {
            val user = repository.loginOrCreateUser(phone, name, role = currentRole.value)
            // Set default address if it exists
            repository.dao.getDefaultAddress(user.id)?.let {
                userAddress.value = it.fullAddress
            }
        }
    }

    fun logoutUser() {
        userAddress.value = "Tap to set delivery location"
        viewModelScope.launch {
            repository.dao.clearUsers()
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            val phone = currentUserPhone.value
            if (phone.isNotEmpty()) {
                repository.dao.updateUserProfile(phone, name, email)
                try {
                    repository.loginOrCreateUser(phone, name, email)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun saveDeliveryAddress(label: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid < 0) return@launch
            repository.saveAddress(uid, label, address, lat, lng)
            userAddress.value = address
        }
    }

    fun makeAddressDefault(addressId: Int) {
        val uid = currentUserId.value
        if (uid >= 0) {
            viewModelScope.launch {
                repository.setDefaultAddress(uid, addressId)
                // Fetch and update active userAddress to match the new default
                repository.dao.getDefaultAddress(uid)?.let {
                    userAddress.value = it.fullAddress
                }
            }
        }
    }

    fun deleteAddress(addressId: Int) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
        }
    }

    private var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient? = null
    private var locationCallback: com.google.android.gms.location.LocationCallback? = null

    fun startActualLocationTracking() {
        if (fusedLocationClient != null) return
        val context = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PlatformViewModel", "Location permissions not granted, skipping active tracking")
            return
        }

        try {
            val client = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient = client

            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            ).apply {
                setMinUpdateIntervalMillis(1500L)
            }.build()

            val callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    updateDeviceLocation(location.latitude, location.longitude)
                    Log.d("PlatformViewModel", "Real-time location updated: ${location.latitude}, ${location.longitude}")
                    
                    viewModelScope.launch {
                        try {
                            val activeJobs = repository.orders.first().filter {
                                it.driverName == currentUserName.value && it.status == "OutForDelivery"
                            }
                            for (job in activeJobs) {
                                repository.updateOrderRemoteStatus(
                                    job.remoteId,
                                    job.id,
                                    "OutForDelivery",
                                    location.latitude,
                                    location.longitude
                                )
                                Log.d("PlatformViewModel", "Sent actual location for Order #${job.id} to remote server: ${location.latitude}, ${location.longitude}")
                            }
                        } catch (e: Exception) {
                            Log.e("PlatformViewModel", "Error posting live location updates: ${e.message}")
                        }
                    }
                }
            }
            locationCallback = callback
            client.requestLocationUpdates(locationRequest, callback, android.os.Looper.getMainLooper())
            Log.d("PlatformViewModel", "Location updates requested successfully")
        } catch (e: SecurityException) {
            Log.e("PlatformViewModel", "SecurityException requesting location updates: ${e.message}")
        } catch (e: Exception) {
            Log.e("PlatformViewModel", "Exception starting location tracking: ${e.message}")
        }
    }

    fun stopActualLocationTracking() {
        try {
            val client = fusedLocationClient
            val callback = locationCallback
            if (client != null && callback != null) {
                client.removeLocationUpdates(callback)
            }
        } catch (e: Exception) {
            Log.e("PlatformViewModel", "Error stopping location updates: ${e.message}")
        }
        fusedLocationClient = null
        locationCallback = null
    }

    private var customerActiveOrderSyncJob: kotlinx.coroutines.Job? = null

    fun startCustomerActiveOrderSync() {
        if (customerActiveOrderSyncJob != null) return
        customerActiveOrderSyncJob = viewModelScope.launch {
            while (true) {
                val orderId = activeOrderId.value
                if (orderId != null) {
                    try {
                        val localOrder = repository.dao.getOrderById(orderId)
                        val remoteId = localOrder?.remoteId
                        if (remoteId != null) {
                            val response = repository.apiService.trackOrder(remoteId)
                            if (response.isSuccessful && response.body()?.success == true) {
                                val updated = response.body()?.data
                                if (updated != null) {
                                    repository.dao.updateOrderDriverAndStatus(
                                        orderId,
                                        updated.status,
                                        updated.driverName,
                                        updated.driverPhone,
                                        updated.driverLat,
                                        updated.driverLng
                                    )
                                    // Trigger notification if status changed reactively
                                    if (localOrder.status != updated.status) {
                                        val statusMsg = when (updated.status) {
                                            "Accepted" -> "Your order has been accepted by the restaurant! 🍳"
                                            "Preparing" -> "The chef is preparing your delicious meal! 🍜"
                                            "Confirmed" -> "A delivery partner has accepted your order! 🛵"
                                            "Ready" -> "Your food is prepared and waiting for the rider at the restaurant! 🛍️"
                                            "OutForDelivery" -> "Your order is Out for Delivery! Keep track of the rider on the map."
                                            "Delivered" -> "Delivered! Enjoy your meal 🍽️"
                                            else -> "Order status is now: ${updated.status}"
                                        }
                                        com.example.utils.NotificationHelper.showNotification(
                                            getApplication(),
                                            "BiteCraft Order Update",
                                            statusMsg
                                        )
                                    }
                                    // Stop tracking if completed/cancelled
                                    if (updated.status == "Delivered" || updated.status == "Cancelled") {
                                        activeOrderId.value = null
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PlatformViewModel", "Error syncing active order for customer: ${e.message}")
                    }
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun stopCustomerActiveOrderSync() {
        customerActiveOrderSyncJob?.cancel()
        customerActiveOrderSyncJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopActualLocationTracking()
        stopCustomerActiveOrderSync()
        kitchenSyncJob?.cancel()
        riderSyncJob?.cancel()
    }
}
