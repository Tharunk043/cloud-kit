package com.example.viewmodel

import android.app.Application
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

class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)

    // Roles: "Customer", "Kitchen", "Rider", "Admin"
    val currentRole = MutableStateFlow("Customer")

    // Navigation and sub-state parameters
    val selectedRestaurantId = MutableStateFlow<Int?>(null)
    val searchQuery = MutableStateFlow("")
    val activeCategory = MutableStateFlow("All")

    val userAddress = MutableStateFlow("Tap to set delivery location")
    val isGoldMember = MutableStateFlow(false)
    val isDarkTheme = MutableStateFlow(false)

    // --- Auth & User Identity ---
    val isLoggedIn = MutableStateFlow(false)
    val currentUserPhone = MutableStateFlow("")
    val currentUserName = MutableStateFlow("Foodie")
    val currentUserId = MutableStateFlow(-1)

    val savedAddresses: StateFlow<List<SavedAddressEntity>> = currentUserId
        .flatMapLatest { uid ->
            if (uid >= 0) repository.dao.getSavedAddresses(uid)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.dao.getCurrentUser()?.let { user ->
                currentUserPhone.value = user.phone
                currentUserName.value = user.name
                currentUserId.value = user.id
                isLoggedIn.value = true

                // Load default address locally
                repository.dao.getDefaultAddress(user.id)?.let {
                    userAddress.value = it.fullAddress
                }

                // Sync profile & address from remote in the background
                try {
                    val syncedUser = repository.loginOrCreateUser(user.phone, user.name)
                    repository.dao.getDefaultAddress(syncedUser.id)?.let {
                        userAddress.value = it.fullAddress
                    }
                } catch (e: Exception) {
                    // Ignore background sync errors when offline
                }
            }
        }
    }

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

    // Wallet balance
    val walletBalance = walletTx.map { txList ->
        var bal = 0.0
        txList.forEach {
            if (it.type == "Deposit" || it.type == "Refund" || it.type == "Cashback") {
                bal += it.amount
            } else {
                bal -= it.amount
            }
        }
        bal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total family spending
    val totalFamilySpend = familyMembers.map { members ->
        members.sumOf { it.monthlySpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    // Wallet funding
    fun addFunds(amount: Double) {
        viewModelScope.launch {
            repository.addWalletFunds(amount)
        }
    }

    // Membership toggle
    fun toggleGoldMembership() {
        isGoldMember.value = !isGoldMember.value
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
            val user = repository.loginOrCreateUser(phone, name)
            currentUserPhone.value = user.phone
            currentUserName.value = user.name.ifEmpty { name }
            currentUserId.value = user.id
            isLoggedIn.value = true
            
            // Set default address if it exists
            repository.dao.getDefaultAddress(user.id)?.let {
                userAddress.value = it.fullAddress
            }
        }
    }

    fun logoutUser() {
        isLoggedIn.value = false
        currentUserPhone.value = ""
        currentUserName.value = "Foodie"
        currentUserId.value = -1
        userAddress.value = "Tap to set delivery location"
        
        viewModelScope.launch {
            repository.dao.clearUsers()
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
}
