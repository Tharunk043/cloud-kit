package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.data.api.GeminiClient
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DishEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PlatformDao
import com.example.data.local.RestaurantEntity
import com.example.data.local.WalletTransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(private val context: Context) {
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "bitecraft_database.db"
    ).fallbackToDestructiveMigration().build()

    val dao: PlatformDao = db.dao()

    // Exposed Flows
    val restaurants: Flow<List<RestaurantEntity>> = dao.getAllRestaurants()
    val cartItems: Flow<List<CartItemEntity>> = dao.getCartItems()
    val orders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val walletTx: Flow<List<WalletTransactionEntity>> = dao.getWalletTransactions()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getChatMessages()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Automatically check and pre-seed on startup
        repositoryScope.launch {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        // Seed Restaurants if empty
        val currentRestaurants = dao.getAllRestaurants().first()
        if (currentRestaurants.isEmpty()) {
            Log.d("AppRepository", "Database is empty. Pre-seeding delicious full-stack menus...")
            
            val seedRest = listOf(
                RestaurantEntity(
                    id = 1,
                    name = "The Gourmet Lab",
                    description = "Scientific flavor manipulation, smash burgers, premium fries & stellar artisan shakes.",
                    cuisine = "Gourmet Burgers & Shakes",
                    rating = 4.8f,
                    deliveryTime = 18,
                    deliveryFee = 2.49,
                    image = "burger_lab",
                    bannerImage = "burger_banner",
                    address = "Sector 12, Innovation Block, Cloud City",
                    isVeg = false,
                    isPromoted = true,
                    distanceKm = 1.2f,
                    latitude = 12.9715987,
                    longitude = 77.5945627
                ),
                RestaurantEntity(
                    id = 2,
                    name = "Slice & Co.",
                    description = "Hand-stretched sourdough pizzas, loaded calzones, and fresh pesto pasta Bowls.",
                    cuisine = "Artisanal Pizza & Italian",
                    rating = 4.7f,
                    deliveryTime = 25,
                    deliveryFee = 3.99,
                    image = "pizza_slice",
                    bannerImage = "pizza_banner",
                    address = "High Street Boulevard, Lane 4, Cloud City",
                    isVeg = false,
                    isPromoted = false,
                    distanceKm = 2.8f,
                    latitude = 12.978589,
                    longitude = 77.640822
                ),
                RestaurantEntity(
                    id = 3,
                    name = "Noodle Craft",
                    description = "Traditional hand-pulled vegan ramen broth, sichuan dumplings, and wok-kissed delicacies.",
                    cuisine = "Asian Fusion & Ramen",
                    rating = 4.6f,
                    deliveryTime = 22,
                    deliveryFee = 1.99,
                    image = "ramen_bowl",
                    bannerImage = "ramen_banner",
                    address = "Zen Gardens, Phase II, Cloud City",
                    isVeg = true,
                    isPromoted = true,
                    distanceKm = 3.5f,
                    latitude = 12.914142,
                    longitude = 77.610531
                ),
                RestaurantEntity(
                    id = 4,
                    name = "Sweet Treat Desserts",
                    description = "Decadent NY cheesecakes, Belgian chocolate fondants, and scone assortments.",
                    cuisine = "Desserts & Cakes",
                    rating = 4.9f,
                    deliveryTime = 12,
                    deliveryFee = 0.00, // Free Delivery
                    image = "cake_slice",
                    bannerImage = "dessert_banner",
                    address = "The Pastry Hub, Galleria Mall, Cloud City",
                    isVeg = true,
                    isPromoted = false,
                    distanceKm = 0.8f,
                    latitude = 12.956281,
                    longitude = 77.585532
                )
            )
            
            val seedDishes = listOf(
                // The Gourmet Lab (Rest 1)
                DishEntity(
                    id = 101, restaurantId = 1, name = "Truffle Truce Burger", price = 12.99,
                    description = "Rich earthy truffle aioli, dual certified angus smash patties, caramelized balsamic mushrooms, swiss glaze.",
                    image = "truffle_burger", category = "Burgers", isVeg = false, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Truffle Glaze,Extra Patty,Bacon Strips"
                ),
                DishEntity(
                    id = 102, restaurantId = 1, name = "Spicy BBQ Heat Burger", price = 10.99,
                    description = "Fiery house hickory BBQ spread, fresh sliced jalapenos, molten pepper jack cheese, toasted butter roll.",
                    image = "bbq_burger", category = "Burgers", isVeg = false, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Extra Cheese,Diced Onion Crumbles"
                ),
                DishEntity(
                    id = 103, restaurantId = 1, name = "Crinkly Parm Fries", price = 4.99,
                    description = "Super crispy crinkle-cut fries dusted generously with aged Parmesan, rosemary flakes, and white truffle splash.",
                    image = "parm_fries", category = "Starters", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Parm Powder,Cheese Sauce Dip"
                ),
                DishEntity(
                    id = 104, restaurantId = 1, name = "Cosmic Caramel Shake", price = 5.49,
                    description = "Slow-blended gourmet salted caramel, fresh vanilla bean ice cream, golden praline sprinkles.",
                    image = "caramel_shake", category = "Desserts", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Whipped Cream,Chocolate Drizzle"
                ),

                // Slice & Co. (Rest 2)
                DishEntity(
                    id = 201, restaurantId = 2, name = "Burrata Blush Pizza", price = 14.99,
                    description = "Blush marinara reduction base, pull-fresh premium burrata, basil pesto oil injection, cracked pepper.",
                    image = "burrata_pizza", category = "Pizzas", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Basil Oil,Extra Garlic Powder"
                ),
                DishEntity(
                    id = 202, restaurantId = 2, name = "Fiery Diavola Pizza", price = 13.99,
                    description = "Molten mozzarella, artisanal spicy pepperoni medallions, Calabrian hot spread, house fire infused oil.",
                    image = "diavola_pizza", category = "Pizzas", isVeg = false, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Extra Pepperoni,Chili Flakes Sachet"
                ),
                DishEntity(
                    id = 203, restaurantId = 2, name = "Creamy Mushroom Fettuccine", price = 12.49,
                    description = "Handcrafted local ribbon pasta layered beautifully with a velvety reduction of sage, butter, mushroom fold, chives.",
                    image = "mushroom_fettuccine", category = "Mains", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Extra Butter Garlic Crust,More Mushrooms"
                ),

                // Noodle Craft (Rest 3)
                DishEntity(
                    id = 301, restaurantId = 3, name = "Volcano Ramen Bowl", price = 11.99,
                    description = "24hr simmered fiery organic miso base, hand-pulled raw noodles, silken tofu, sheets of nori, spicy pepper dust.",
                    image = "volcano_ramen", category = "Ramen", isVeg = true, isBestseller = true, spiceLevelSupport = true,
                    addonsJson = "Extra Nori Sheets,Soft Egg (Non-Veg)"
                ),
                DishEntity(
                    id = 302, restaurantId = 3, name = "Sichuan Chili Glass Noodles", price = 9.99,
                    description = "Handmade sweet potato glass ribbons doused in highly addictive red hot chili oil crunch, green onions, sesame.",
                    image = "glass_noodles", category = "Mains", isVeg = true, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Tofu Cubes,Sesame Sprinkles"
                ),
                DishEntity(
                    id = 303, restaurantId = 3, name = "Veggie Gyoza Platter", price = 5.99,
                    description = "Pan-seared crystal transparent dumplings packed with mixed local greens, glass noodle trim, ginger-soy.",
                    image = "veggie_gyoza", category = "Starters", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Chili Crisp Oil Cup,Soy dipping sauce"
                ),

                // Sweet Treat Desserts (Rest 4)
                DishEntity(
                    id = 401, restaurantId = 4, name = "Luxe Lava Chocolate Fondant", price = 7.99,
                    description = "Baked-to-order Single-origin dark chocolate cake with a gushing molten core, paired with a scoop of premium vanilla bean.",
                    image = "lava_fondant", category = "Cakes", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Fudge Injection,Extra Ice Cream Scoop"
                ),
                DishEntity(
                    id = 402, restaurantId = 4, name = "Salted Caramel Pecan Cheesecake", price = 6.49,
                    description = "Silky smooth New York sour-cream style cheesecake base over a thick graham crust, loaded with glazed toasted pecans.",
                    image = "pecan_cheesecake", category = "Cakes", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Extra Salted Caramel Drizzle"
                ),
                DishEntity(
                    id = 403, restaurantId = 4, name = "Matcha White Choc Scone", price = 3.99,
                    description = "Delicately baked crumbly scone blended with real Japanese Kyoto matcha powder and creamy white chocolate chips.",
                    image = "matcha_scone", category = "Starters", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Clotted Cream Spoon"
                )
            )

            // Wallet seed transaction
            val seedTx = WalletTransactionEntity(
                type = "Deposit",
                amount = 50.00,
                description = "Starter welcome cash bonus credited!",
                timestamp = System.currentTimeMillis()
            )

            dao.insertRestaurants(seedRest)
            dao.insertDishes(seedDishes)
            dao.insertWalletTransaction(seedTx)
            Log.d("AppRepository", "Seed complete! Inserted ${seedRest.size} restaurants and ${seedDishes.size} dishes.")
        }
    }

    // Custom Helpers
    suspend fun getWalletBalance(): Double = withContext(Dispatchers.IO) {
        val txs = dao.getWalletTransactions().first()
        var balance = 0.0
        txs.forEach {
            if (it.type == "Deposit" || it.type == "Refund" || it.type == "Cashback") {
                balance += it.amount
            } else {
                balance -= it.amount
            }
        }
        balance
    }

    suspend fun addWalletFunds(amount: Double) = withContext(Dispatchers.IO) {
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                type = "Deposit",
                amount = amount,
                description = "Wallet fund deposit via UPI/Card",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addCartItem(dish: DishEntity, quantity: Int, spice: String, addons: String, notes: String) = withContext(Dispatchers.IO) {
        val rest = dao.getRestaurantById(dish.restaurantId) ?: return@withContext
        val existing = dao.getCartItems().first()
        val match = existing.find { it.dishId == dish.id && it.spiceLevel == spice && it.addons == addons }
        if (match != null) {
            dao.updateCartQuantity(match.id, match.quantity + quantity)
        } else {
            dao.insertCartItem(
                CartItemEntity(
                    dishId = dish.id,
                    restaurantId = dish.restaurantId,
                    restaurantName = rest.name,
                    name = dish.name,
                    price = dish.price,
                    quantity = quantity,
                    spiceLevel = spice,
                    addons = addons,
                    notes = notes
                )
            )
        }
    }

    suspend fun checkout(
        paymentMethod: String,
        address: String,
        promoCode: String = "",
        userLat: Double? = null,
        userLng: Double? = null
    ): Int = withContext(Dispatchers.IO) {
        val items = dao.getCartItems().first()
        if (items.isEmpty()) return@withContext -1

        val restId = items.first().restaurantId
        val restName = items.first().restaurantName
        
        var subtotal = 0.0
        val summaryBuilder = StringBuilder()
        val detailArray = JSONArray()

        items.forEachIndexed { idx, it ->
            val cost = it.price * it.quantity
            subtotal += cost
            summaryBuilder.append("${it.name} x${it.quantity}")
            if (idx != items.lastIndex) summaryBuilder.append(", ")

            val detailObj = JSONObject()
            detailObj.put("name", it.name)
            detailObj.put("price", it.price)
            detailObj.put("quantity", it.quantity)
            detailObj.put("customs", "${it.spiceLevel}, ${it.addons}")
            detailArray.put(detailObj)
        }

        val restaurant = dao.getRestaurantById(restId)
        val deliveryFee = restaurant?.deliveryFee ?: 2.0
        var discount = 0.0
        if (promoCode == "GOLD100") {
            discount = subtotal.coerceAtMost(10.0) // Cap promo discount at $10.00
        }

        val totalAmount = subtotal + deliveryFee - discount

        // Deduct from wallet if wallet selected
        if (paymentMethod == "Wallet") {
            val balance = getWalletBalance()
            if (balance < totalAmount) {
                return@withContext -2 // Insufficient funds symbol
            }
            // Add negative tx
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    type = "Debit",
                    amount = totalAmount,
                    description = "Paid for Order at $restName",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        val resolvedCustomerLat = userLat ?: 12.935123
        val resolvedCustomerLng = userLng ?: 77.624128

        val rawRestaurantLat = restaurant?.latitude ?: 12.9715987
        val rawRestaurantLng = restaurant?.longitude ?: 77.5945627

        // If the user's real GPS is far from seed coordinates, spawn restaurant locally (approx 1 km away)
        val isFar = if (userLat != null && userLng != null) {
            val distLat = Math.abs(rawRestaurantLat - userLat)
            val distLng = Math.abs(rawRestaurantLng - userLng)
            distLat > 0.5 || distLng > 0.5
        } else {
            false
        }

        val resolvedRestaurantLat = if (isFar && userLat != null) {
            userLat + 0.0068
        } else {
            rawRestaurantLat
        }

        val resolvedRestaurantLng = if (isFar && userLng != null) {
            userLng - 0.0072
        } else {
            rawRestaurantLng
        }

        val newOrder = OrderEntity(
            restaurantId = restId,
            restaurantName = restName,
            status = "Placed",
            totalAmount = totalAmount,
            itemsSummary = summaryBuilder.toString(),
            itemsDetailJson = detailArray.toString(),
            paymentMethod = paymentMethod,
            deliveryAddress = address,
            timestamp = System.currentTimeMillis(),
            driverName = "Dash Rider",
            driverPhone = "+1 415-555-5382",
            driverLat = resolvedRestaurantLat, // Seed at restaurant coordinates
            driverLng = resolvedRestaurantLng,
            customerLat = resolvedCustomerLat,
            customerLng = resolvedCustomerLng,
            restaurantLat = resolvedRestaurantLat,
            restaurantLng = resolvedRestaurantLng,
            ratingGiven = 0f,
            reviewText = "",
            reviewSentiment = "Neutral"
        )

        val orderId = dao.insertOrder(newOrder).toInt()
        
        // Clear cart
        dao.clearCart()

        // Spin up order tracking simulation process in background!
        simulateOrderProgress(orderId)

        orderId
    }

    private fun simulateOrderProgress(orderId: Int) {
        repositoryScope.launch {
            // Live updates simulated like websockets/Socket.IO updates
            delay(4000)
            dao.updateOrderStatus(orderId, "Accepted")
            
            delay(5000)
            dao.updateOrderStatus(orderId, "Preparing")
            
            delay(7000)
            dao.updateOrderStatus(orderId, "OutForDelivery")

            // Assign a random rider name
            val riderNames = listOf("Alex Rider", "Jordan Transporter", "Sam Courier", "Taylor Direct")
            val selectedRider = riderNames.random()
            
            // Fetch order driver properties
            val order = dao.getOrderById(orderId)
            if (order != null) {
                val startLat = order.restaurantLat
                val startLng = order.restaurantLng
                val endLat = order.customerLat
                val endLng = order.customerLng
                
                val steps = 15
                for (i in 1..steps) {
                    val fraction = i.toDouble() / steps
                    val currentLat = startLat + (endLat - startLat) * fraction
                    val currentLng = startLng + (endLng - startLng) * fraction
                    
                    delay(2500)
                    dao.updateDriverLocation(orderId, currentLat, currentLng)
                }
            }

            dao.updateOrderStatus(orderId, "Delivered")
            
            // Add a little dynamic cashback refund simulation to Loyalty Points / Wallet if user has Gold!
            // Let's credit the cashback to wallet automatically
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    type = "Cashback",
                    amount = 1.00,
                    description = "10% BiteCraft standard order cashback!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addPromoTextReview(orderId: Int, rating: Float, reviewText: String) = withContext(Dispatchers.IO) {
        val sentiment = GeminiClient.analyzeSentiment(reviewText)
        dao.submitOrderReview(orderId, rating, reviewText, sentiment)
        
        // Credit cashback if review is Positive to loyalty program
        if (sentiment == "Positive") {
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    type = "Cashback",
                    amount = 2.00, // Refund or credit positive loyalty feedback!
                    description = "Loyalty feedback bonus for order #$orderId!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun insertChatMessage(sender: String, message: String) = withContext(Dispatchers.IO) {
        dao.insertChatMessage(ChatMessageEntity(sender = sender, message = message, timestamp = System.currentTimeMillis()))
        
        // Let chatBot respond
        val allHistory = dao.getChatMessages().first()
        val botReply = GeminiClient.chatWithAiSupport(allHistory, message)
        
        dao.insertChatMessage(ChatMessageEntity(sender = "Assistant", message = botReply, timestamp = System.currentTimeMillis()))
    }
}
