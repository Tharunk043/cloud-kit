package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.CartItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DishEntity
import com.example.data.local.OrderEntity
import com.example.data.local.RestaurantEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)

    // Roles: "Customer", "Kitchen", "Rider", "Admin"
    val currentRole = MutableStateFlow("Customer")

    // Navigation and sub-state parameters
    val selectedRestaurantId = MutableStateFlow<Int?>(null)
    val searchQuery = MutableStateFlow("")
    val activeCategory = MutableStateFlow("All")
    
    val userAddress = MutableStateFlow("Suite 301, Innovation Block, Cloud City")
    val isGoldMember = MutableStateFlow(false)
    val isDarkTheme = MutableStateFlow(false)

    val deviceLatitude = MutableStateFlow<Double?>(null)
    val deviceLongitude = MutableStateFlow<Double?>(null)

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

    // Observe DB records continuously using combine for listings
    val rawRestaurants: StateFlow<List<RestaurantEntity>> = repository.restaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRestaurants = combine(rawRestaurants, searchQuery, activeCategory) { rests, query, cat ->
        rests.filter { rest ->
            val matchesQuery = rest.name.contains(query, ignoreCase = true) || 
                               rest.cuisine.contains(query, ignoreCase = true)
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

    // Checkout
    fun placeOrder(paymentMethod: String, address: String, userLat: Double? = null, userLng: Double? = null, callback: (Int) -> Unit) {
        viewModelScope.launch {
            val promo = if (isGoldMember.value) "GOLD100" else ""
            val orderId = repository.checkout(paymentMethod, address, promo, userLat, userLng)
            if (orderId > 0) {
                activeOrderId.value = orderId
            }
            callback(orderId)
        }
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
}
