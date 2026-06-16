package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.utils.NotificationHelper
import androidx.room.Room
import com.example.data.api.GeminiClient
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DishEntity
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.FamilyTransactionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.PlatformDao
import com.example.data.local.RestaurantEntity
import com.example.data.local.SavedAddressEntity
import com.example.data.local.UserEntity
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

enum class PaymentType {
    WALLET, UPI, CARD, COD, FAMILY_WALLET, SPLIT_BILL
}

data class SplitBillEntry(
    val memberId: Int,
    val memberName: String,
    val amount: Double
)

class AppRepository(private val context: Context) {
    // Retrofit instance to connect to Render hosted backend
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://cloud-kit.onrender.com/")
        .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(
            com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
        ))
        .client(
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val credentials = okhttp3.Credentials.basic("tharun", "Tharunk043")
                    val request = chain.request().newBuilder()
                        .header("Authorization", credentials)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        .build()

    val apiService: com.example.data.api.MongoApiService = retrofit.create(com.example.data.api.MongoApiService::class.java)
    private val mongoOrderIdMap = java.util.concurrent.ConcurrentHashMap<Int, String>()

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "bitecraft_database.db"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    val dao: PlatformDao = db.dao()

    // Exposed Flows
    val restaurants: Flow<List<RestaurantEntity>> = dao.getAllRestaurants()
    val cartItems: Flow<List<CartItemEntity>> = dao.getCartItems()
    val orders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val walletTx: Flow<List<WalletTransactionEntity>> = dao.getWalletTransactions()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getChatMessages()
    val familyMembers: Flow<List<FamilyMemberEntity>> = dao.getFamilyMembers()
    val familyTransactions: Flow<List<FamilyTransactionEntity>> = dao.getFamilyTransactions()
    val paymentMethods: Flow<List<PaymentMethodEntity>> = dao.getPaymentMethods()
    val categories: Flow<List<com.example.data.local.CategoryEntity>> = dao.getAllCategories()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        val currentCategories = dao.getAllCategories().first()
        if (currentCategories.isEmpty()) {
            Log.d("AppRepository", "Categories database is empty. Seeding premium categories...")
            val seedCats = listOf(
                com.example.data.local.CategoryEntity("burgers", "Burgers", "ic_category_burger", "#FFFFEAD2"),
                com.example.data.local.CategoryEntity("pizza", "Pizza", "ic_category_pizza", "#FFFFF1EB"),
                com.example.data.local.CategoryEntity("sushi", "Sushi", "ic_category_sushi", "#FFE4F0EC"),
                com.example.data.local.CategoryEntity("healthy", "Healthy", "ic_category_healthy", "#FFE2F3E7"),
                com.example.data.local.CategoryEntity("desserts", "Desserts", "ic_category_dessert", "#FFFFF0F5"),
                com.example.data.local.CategoryEntity("ramen", "Ramen", "ic_category_ramen", "#FFFFEAD2")
            )
            dao.insertCategories(seedCats)
        }

        val currentRestaurants = dao.getAllRestaurants().first()
        if (currentRestaurants.isEmpty()) {
            Log.d("AppRepository", "Database is empty. Pre-seeding delicious full-stack menus...")

            val seedRest = listOf(
                RestaurantEntity(
                    id = 1,
                    name = "Rayalaseema Ruchulu Kadapa",
                    description = "Authentic spicy Rayalaseema cuisine — ragi sangati, gongura mutton, chepa pulusu & traditional Kadapa meals.",
                    cuisine = "Andhra & Rayalaseema",
                    rating = 4.8f,
                    deliveryTime = 20,
                    deliveryFee = 25.0,
                    image = "burger_lab",
                    bannerImage = "burger_banner",
                    address = "Seven Roads Circle, Kadapa, Andhra Pradesh 516001",
                    isVeg = false,
                    isPromoted = true,
                    distanceKm = 1.2f,
                    latitude = 14.4745,
                    longitude = 78.8262
                ),
                RestaurantEntity(
                    id = 2,
                    name = "Hotel Srinivasa Regency",
                    description = "Legendary Rayalaseema spiced dum biryani, nalli shorba, mirchi bajji and traditional kebabs.",
                    cuisine = "Biryani & Mughlai",
                    rating = 4.7f,
                    deliveryTime = 30,
                    deliveryFee = 30.0,
                    image = "pizza_slice",
                    bannerImage = "pizza_banner",
                    address = "Trunk Road, Near RTC Bus Stand, Kadapa, Andhra Pradesh 516001",
                    isVeg = false,
                    isPromoted = false,
                    distanceKm = 2.8f,
                    latitude = 14.4752,
                    longitude = 78.8258
                ),
                RestaurantEntity(
                    id = 3,
                    name = "Govinda Pure Veg Kadapa",
                    description = "Pure vegetarian South Indian meals — pesarattu, punugulu, gongura pachadi, special thali & filter coffee.",
                    cuisine = "South Indian Vegetarian",
                    rating = 4.6f,
                    deliveryTime = 18,
                    deliveryFee = 20.0,
                    image = "ramen_bowl",
                    bannerImage = "ramen_banner",
                    address = "Yerramukkapalli, Kadapa, Andhra Pradesh 516004",
                    isVeg = true,
                    isPromoted = true,
                    distanceKm = 3.5f,
                    latitude = 14.4645,
                    longitude = 78.8152
                ),
                RestaurantEntity(
                    id = 4,
                    name = "Haritha Tourism Hotel",
                    description = "Famous regional sweets — Tirupati laddu, Ariselu, Pootharekulu, Bobbatlu & traditional snacks.",
                    cuisine = "Indian Sweets & Snacks",
                    rating = 4.9f,
                    deliveryTime = 12,
                    deliveryFee = 0.0,
                    image = "cake_slice",
                    bannerImage = "dessert_banner",
                    address = "Near Collectorate, Kadapa, Andhra Pradesh 516001",
                    isVeg = true,
                    isPromoted = false,
                    distanceKm = 0.8f,
                    latitude = 14.4784,
                    longitude = 78.8192
                ),
                RestaurantEntity(
                    id = 5,
                    name = "Hyderabad Biryani House Kadapa",
                    description = "Premium Hyderabadi & Rayalaseema fusion cuisine — Kacchi dum biryani, double ka meetha, Irani chai & mutton specialties.",
                    cuisine = "Hyderabadi",
                    rating = 4.7f,
                    deliveryTime = 25,
                    deliveryFee = 35.0,
                    image = "burger_lab",
                    bannerImage = "ramen_banner",
                    address = "Christian Lane, Kadapa, Andhra Pradesh 516001",
                    isVeg = false,
                    isPromoted = true,
                    distanceKm = 4.2f,
                    latitude = 14.4718,
                    longitude = 78.8235
                )
            )

            // Try to sync restaurants list from Node.js MongoDB Atlas database
            try {
                val response = apiService.getRestaurants(page = 0, size = 50)
                if (response.isSuccessful && response.body()?.success == true) {
                    val mongoRestaurants = response.body()?.data ?: emptyList()
                    if (mongoRestaurants.isNotEmpty()) {
                        val entities = mongoRestaurants.map {
                            RestaurantEntity(
                                id = it.id.toIntOrNull() ?: 1,
                                name = it.name,
                                description = it.description,
                                cuisine = it.cuisine,
                                rating = it.rating,
                                deliveryTime = it.deliveryTime,
                                deliveryFee = it.deliveryFee,
                                image = it.imageUrl,
                                bannerImage = if (it.imageUrl == "burger_lab") "burger_banner" else if (it.imageUrl == "pizza_slice") "pizza_banner" else if (it.imageUrl == "ramen_bowl") "ramen_banner" else "dessert_banner",
                                address = it.address,
                                isVeg = it.isVeg,
                                isPromoted = it.isPromoted,
                                distanceKm = 1.2f,
                                latitude = it.latitude,
                                longitude = it.longitude
                            )
                        }
                        dao.clearRestaurants()
                        dao.insertRestaurants(entities)
                        Log.d("AppRepository", "Successfully synced restaurants from MongoDB Atlas database!")
                    } else {
                        dao.insertRestaurants(seedRest)
                    }
                } else {
                    dao.insertRestaurants(seedRest)
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Failed to contact MongoDB backend, falling back to local pre-seeding: ${e.message}")
                dao.insertRestaurants(seedRest)
            }


            val seedDishes = listOf(
                // Restaurant 1 - Rayalaseema Ruchulu
                DishEntity(id = 101, restaurantId = 1, name = "Gongura Mutton Curry", price = 220.0,
                    description = "Slow-cooked tender mutton in tangy sorrel (gongura) gravy — the crown jewel of Rayalaseema cuisine.",
                    image = "truffle_burger", category = "Main Course", isVeg = false, isBestseller = true, spiceLevelSupport = true,
                    addonsJson = "Extra Gongura,Bone-In Pieces,Extra Gravy"),
                DishEntity(id = 102, restaurantId = 1, name = "Ragi Sangati Meal", price = 150.0,
                    description = "Traditional Rayalaseema finger millet balls served with spicy pappu charu, gongura pachadi & roasted papad.",
                    image = "bbq_burger", category = "Thali & Meals", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Extra Sangati Ball,Egg Curry Add-on"),
                DishEntity(id = 103, restaurantId = 1, name = "Chepa Pulusu", price = 180.0,
                    description = "Tangy tamarind fish curry with Rohu fish, Andhra spices — best paired with steamed rice.",
                    image = "parm_fries", category = "Seafood", isVeg = false, isBestseller = true, spiceLevelSupport = true,
                    addonsJson = "Extra Fish Pieces,Steamed Rice"),
                DishEntity(id = 104, restaurantId = 1, name = "Andhra Chicken Fry", price = 280.0,
                    description = "Dry-spiced whole chicken pieces tossed with fresh curry leaves, guntur mirchi & aromatic spice blend.",
                    image = "caramel_shake", category = "Starters", isVeg = false, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Extra Masala,Mint Chutney"),
                // Restaurant 2 - Biryani House Guntur
                DishEntity(id = 201, restaurantId = 2, name = "Guntur Chicken Biryani", price = 199.0,
                    description = "Aromatic basmati rice layered with spicy Guntur-style chicken, saffron, crispy onions & Andhra spice masala.",
                    image = "burrata_pizza", category = "Biryani", isVeg = false, isBestseller = true, spiceLevelSupport = true,
                    addonsJson = "Extra Raita,Extra Chicken Piece,Mirchi Salan"),
                DishEntity(id = 202, restaurantId = 2, name = "Mutton Dum Biryani", price = 280.0,
                    description = "Slow-cooked dum biryani with tender mutton, rose water, fried onion & kewra essence.",
                    image = "diavola_pizza", category = "Biryani", isVeg = false, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Extra Mutton,Brinjal Salan,Boiled Egg"),
                DishEntity(id = 203, restaurantId = 2, name = "Hyderabadi Haleem", price = 150.0,
                    description = "Slow-cooked wheat & mutton haleem with caramelized onions, lime, ginger julienne & fried onions.",
                    image = "mushroom_fettuccine", category = "Starters", isVeg = false, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Lime,Ghee Drizzle"),
                // Restaurant 3 - Govinda's Pure Veg
                DishEntity(id = 301, restaurantId = 3, name = "Pesarattu Combo", price = 80.0,
                    description = "Crispy green moong dal dosas served with ginger chutney, allam pachadi & upma stuffing.",
                    image = "volcano_ramen", category = "Breakfast", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Upma Stuffing,Extra Chutney,Filter Coffee"),
                DishEntity(id = 302, restaurantId = 3, name = "Andhra Meals Thali", price = 120.0,
                    description = "Full Andhra thali: rice, sambar, rasam, dal, vegetable curry, pickle, papad & curd.",
                    image = "glass_noodles", category = "Thali & Meals", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Rice,Ghee,Dessert Add-on"),
                DishEntity(id = 303, restaurantId = 3, name = "Gongura Paneer", price = 160.0,
                    description = "Fresh paneer cubes cooked in a spicy tangy gongura sauce with Andhra tempering.",
                    image = "veggie_gyoza", category = "Main Course", isVeg = true, isBestseller = false, spiceLevelSupport = true,
                    addonsJson = "Extra Paneer,Butter Naan"),
                // Restaurant 4 - Sweet Bhoomi Sweets
                DishEntity(id = 401, restaurantId = 4, name = "Pootharekulu (4 pcs)", price = 80.0,
                    description = "Andhra's iconic paper-thin rice starch sweet filled with jaggery, cardamom & nuts. Melts in your mouth.",
                    image = "lava_fondant", category = "Traditional Sweets", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Dry Fruit Filling,Chocolate Filling"),
                DishEntity(id = 402, restaurantId = 4, name = "Tirupati Laddu (2 pcs)", price = 60.0,
                    description = "Authentic Tirupati style besan laddus made with pure ghee, sugar & cardamom.",
                    image = "pecan_cheesecake", category = "Traditional Sweets", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Laddu"),
                DishEntity(id = 403, restaurantId = 4, name = "Ariselu (6 pcs)", price = 70.0,
                    description = "Crispy deep-fried rice & jaggery sweet discs — traditional Sankranti Andhra delicacy.",
                    image = "matcha_scone", category = "Traditional Sweets", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Sesame Coating"),
                // Restaurant 5 - Hyderabad Spice Garden
                DishEntity(id = 501, restaurantId = 5, name = "Kacchi Dum Biryani", price = 320.0,
                    description = "Royal Hyderabadi biryani with marinated raw chicken slow-cooked under sealed dough in aromatic basmati.",
                    image = "truffle_burger", category = "Biryani", isVeg = false, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Mirchi Salan,Extra Raita,Boiled Egg"),
                DishEntity(id = 502, restaurantId = 5, name = "Irani Chai + Osmania Biscuits", price = 60.0,
                    description = "Classic Hyderabadi Irani chai with strong tea & milk, served with crunchy Osmania biscuits.",
                    image = "caramel_shake", category = "Beverages", isVeg = true, isBestseller = true, spiceLevelSupport = false,
                    addonsJson = "Extra Biscuits,Sugar-Free Option"),
                DishEntity(id = 503, restaurantId = 5, name = "Double Ka Meetha", price = 90.0,
                    description = "Hyderabadi bread pudding with fried bread slices, condensed milk, saffron, dry fruits & rose water.",
                    image = "lava_fondant", category = "Desserts", isVeg = true, isBestseller = false, spiceLevelSupport = false,
                    addonsJson = "Ice Cream Scoop,Extra Dry Fruits")
            )

            val seedTx = WalletTransactionEntity(
                type = "Deposit",
                amount = 50.00,
                description = "Starter welcome cash bonus credited!",
                timestamp = System.currentTimeMillis()
            )

            // Seed family members
            val seedFamilyMembers = listOf(
                FamilyMemberEntity(name = "Tharun V", email = "tharun@bitecraft.in", avatarColor = 0xFFFC8019, spendingLimit = 200.0, monthlySpent = 45.50, isAdmin = true),
                FamilyMemberEntity(name = "Priya V", email = "priya@bitecraft.in", avatarColor = 0xFF7C3AED, spendingLimit = 150.0, monthlySpent = 32.00, isAdmin = false),
                FamilyMemberEntity(name = "Ravi V", email = "ravi@bitecraft.in", avatarColor = 0xFF059669, spendingLimit = 100.0, monthlySpent = 18.75, isAdmin = false)
            )

            // Seed payment methods
            val seedPaymentMethods = listOf(
                PaymentMethodEntity(type = "UPI", label = "tharun@paytm", maskedValue = "tharun@paytm", isDefault = true),
                PaymentMethodEntity(type = "UPI", label = "tharun@gpay", maskedValue = "tharun@gpay", isDefault = false),
                PaymentMethodEntity(type = "CARD", label = "Visa ****4242", maskedValue = "****4242", isDefault = false)
            )

            dao.insertRestaurants(seedRest)
            dao.insertDishes(seedDishes)
            dao.insertWalletTransaction(seedTx)
            seedFamilyMembers.forEach { dao.insertFamilyMember(it) }
            seedPaymentMethods.forEach { dao.insertPaymentMethod(it) }
            Log.d("AppRepository", "Seed complete! Inserted ${seedRest.size} restaurants, ${seedDishes.size} dishes, ${seedFamilyMembers.size} family members.")
        }
    }

    // Custom Helpers
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
        userLng: Double? = null,
        splitEntries: List<SplitBillEntry> = emptyList()
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
            discount = subtotal.coerceAtMost(10.0)
        }

        val totalAmount = subtotal + deliveryFee - discount

        // Handle payment type
        when (paymentMethod) {
            "Wallet" -> {
                val balance = getWalletBalance()
                if (balance < totalAmount) return@withContext -2
                dao.insertWalletTransaction(
                    WalletTransactionEntity(
                        type = "Debit",
                        amount = totalAmount,
                        description = "Paid for Order at $restName",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "Family Wallet" -> {
                // Deduct from family admin's wallet
                val balance = getWalletBalance()
                if (balance < totalAmount) return@withContext -2
                dao.insertWalletTransaction(
                    WalletTransactionEntity(
                        type = "Debit",
                        amount = totalAmount,
                        description = "Family Wallet payment at $restName",
                        timestamp = System.currentTimeMillis(),
                        memberName = "Family Account"
                    )
                )
            }
            "Split Bill" -> {
                // Handle split bill between family members
                if (splitEntries.isNotEmpty()) {
                    splitEntries.forEach { entry ->
                        dao.insertFamilyTransaction(
                            FamilyTransactionEntity(
                                memberId = entry.memberId,
                                memberName = entry.memberName,
                                orderId = 0, // will update after order creation
                                amount = entry.amount,
                                description = "Split bill at $restName"
                            )
                        )
                        dao.incrementMemberSpending(entry.memberId, entry.amount)
                    }
                }
            }
        }

        val resolvedCustomerLat = userLat ?: 12.935123
        val resolvedCustomerLng = userLng ?: 77.624128

        val rawRestaurantLat = restaurant?.latitude ?: 12.9715987
        val rawRestaurantLng = restaurant?.longitude ?: 77.5945627

        val distLat = Math.abs(rawRestaurantLat - resolvedCustomerLat)
        val distLng = Math.abs(rawRestaurantLng - resolvedCustomerLng)
        val isFar = distLat > 0.5 || distLng > 0.5

        val resolvedRestaurantLat = if (isFar) resolvedCustomerLat + 0.0068 else rawRestaurantLat
        val resolvedRestaurantLng = if (isFar) resolvedCustomerLng - 0.0072 else rawRestaurantLng

        var localOrderId = 0

        val user = dao.getCurrentUser()
        try {
            val itemsList = items.map {
                com.example.data.api.MongoOrderItem(
                    dishId = "${it.dishId}",
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price
                )
            }
            val placeRequest = com.example.data.api.PlaceOrderRequest(
                userId = user?.phone,
                restaurantId = "$restId",
                items = itemsList,
                totalAmount = totalAmount,
                deliveryAddress = address,
                paymentMethod = paymentMethod,
                customerLat = resolvedCustomerLat,
                customerLng = resolvedCustomerLng,
                restaurantLat = resolvedRestaurantLat,
                restaurantLng = resolvedRestaurantLng
            )
            
            val response = apiService.placeOrder(placeRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val mongoOrder = response.body()?.data
                if (mongoOrder != null) {
                    val newOrder = OrderEntity(
                        restaurantId = restId,
                        restaurantName = restName,
                        status = mongoOrder.status,
                        totalAmount = totalAmount,
                        itemsSummary = summaryBuilder.toString(),
                        itemsDetailJson = detailArray.toString(),
                        paymentMethod = paymentMethod,
                        deliveryAddress = address,
                        timestamp = mongoOrder.createdAt,
                        driverName = mongoOrder.driverName,
                        driverPhone = mongoOrder.driverPhone,
                        driverLat = mongoOrder.driverLat,
                        driverLng = mongoOrder.driverLng,
                        customerLat = resolvedCustomerLat,
                        customerLng = resolvedCustomerLng,
                        restaurantLat = resolvedRestaurantLat,
                        restaurantLng = resolvedRestaurantLng,
                        ratingGiven = 0f,
                        reviewText = "",
                        reviewSentiment = "Neutral",
                        remoteId = mongoOrder.id
                    )
                    localOrderId = dao.insertOrder(newOrder).toInt()
                    mongoOrderIdMap[localOrderId] = mongoOrder.id
                    dao.clearCart()
                    
                    if (user != null) {
                        syncWalletTransactions(user.phone)
                    }

                    startServerDrivenTracking(localOrderId, mongoOrder.id)
                    return@withContext localOrderId
                }
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to checkout on Supabase backend, using local simulation fallback: ${e.message}")
        }

        // FALLBACK: If API fails, run the original local Room simulation
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
            driverLat = resolvedRestaurantLat,
            driverLng = resolvedRestaurantLng,
            customerLat = resolvedCustomerLat,
            customerLng = resolvedCustomerLng,
            restaurantLat = resolvedRestaurantLat,
            restaurantLng = resolvedRestaurantLng,
            ratingGiven = 0f,
            reviewText = "",
            reviewSentiment = "Neutral"
        )

        localOrderId = dao.insertOrder(newOrder).toInt()
        dao.clearCart()
        simulateOrderProgress(localOrderId)
        localOrderId
    }

    private fun startServerDrivenTracking(localOrderId: Int, mongoOrderId: String) {
        repositoryScope.launch {
            var isTracking = true
            var lastStatus = ""
            while (isTracking) {
                delay(4000)
                try {
                    val response = apiService.trackOrder(mongoOrderId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val updatedOrder = response.body()?.data
                        if (updatedOrder != null) {
                            if (updatedOrder.status != lastStatus) {
                                lastStatus = updatedOrder.status
                                NotificationHelper.showNotification(
                                    context,
                                    "Order Updates",
                                    "Order status is now: ${updatedOrder.status}"
                                )
                            }
                             dao.updateOrderDriverAndStatus(
                                 localOrderId,
                                 updatedOrder.status,
                                 updatedOrder.driverName,
                                 updatedOrder.driverPhone,
                                 updatedOrder.driverLat,
                                 updatedOrder.driverLng
                             )
                             Log.d("AppRepository", "Order track sync: status=${updatedOrder.status}, driver=${updatedOrder.driverName}, driverLat=${updatedOrder.driverLat}, driverLng=${updatedOrder.driverLng}")
                            
                            if (updatedOrder.status == "Delivered" || updatedOrder.status == "Cancelled") {
                                isTracking = false
                                if (updatedOrder.status == "Delivered") {
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
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AppRepository", "Error tracking order from remote backend: ${e.message}")
                }
            }
        }
    }

    private fun simulateOrderProgress(orderId: Int) {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.utils.OrderSimulationWorker>()
            .setInputData(androidx.work.workDataOf("order_id" to orderId))
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
    }

    suspend fun addPromoTextReview(orderId: Int, rating: Float, reviewText: String) = withContext(Dispatchers.IO) {
        val sentiment = GeminiClient.analyzeSentiment(reviewText)
        dao.submitOrderReview(orderId, rating, reviewText, sentiment)

        val uuid = mongoOrderIdMap[orderId]
        if (uuid != null) {
            try {
                apiService.submitOrderReview(
                    uuid,
                    com.example.data.api.SubmitReviewRequest(
                        rating = rating,
                        reviewText = reviewText,
                        sentiment = sentiment
                    )
                )
                Log.d("AppRepository", "Submitted order review to remote Supabase DB successfully")
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to submit review to remote Supabase DB: ${e.message}")
            }
        }

        if (sentiment == "Positive") {
            val user = dao.getCurrentUser()
            if (user != null) {
                try {
                    apiService.addWalletTransaction(
                        user.phone,
                        com.example.data.api.AddWalletTxRequest(
                            type = "Cashback",
                            amount = 2.00,
                            description = "Loyalty feedback bonus for order #$orderId!"
                        )
                    )
                    syncWalletTransactions(user.phone)
                } catch (e: Exception) {
                    Log.w("AppRepository", "Failed to sync cashback wallet transaction: ${e.message}")
                }
            }
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    type = "Cashback",
                    amount = 2.00,
                    description = "Loyalty feedback bonus for order #$orderId!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun insertChatMessage(sender: String, message: String) = withContext(Dispatchers.IO) {
        dao.insertChatMessage(ChatMessageEntity(sender = sender, message = message, timestamp = System.currentTimeMillis()))

        val allHistory = dao.getChatMessages().first()
        val botReply = GeminiClient.chatWithAiSupport(allHistory, message)

        dao.insertChatMessage(ChatMessageEntity(sender = "Assistant", message = botReply, timestamp = System.currentTimeMillis()))
    }

    // --- Family Management ---
    suspend fun addFamilyMember(name: String, email: String, spendingLimit: Double, avatarColor: Long) = withContext(Dispatchers.IO) {
        dao.insertFamilyMember(
            FamilyMemberEntity(
                name = name,
                email = email,
                avatarColor = avatarColor,
                spendingLimit = spendingLimit,
                monthlySpent = 0.0,
                isAdmin = false
            )
        )
        Log.d("AppRepository", "Added family member: $name")
    }

    suspend fun removeFamilyMember(id: Int) = withContext(Dispatchers.IO) {
        dao.deactivateMember(id)
    }

    suspend fun updateMemberSpendingLimit(id: Int, limit: Double) = withContext(Dispatchers.IO) {
        dao.updateMemberSpendingLimit(id, limit)
    }

    suspend fun getMemberCount(): Int = withContext(Dispatchers.IO) {
        dao.getFamilyMemberCount()
    }

    suspend fun getTotalFamilySpend(): Double = withContext(Dispatchers.IO) {
        dao.getTotalFamilySpending() ?: 0.0
    }

    // --- Payment Methods ---
    suspend fun addPaymentMethod(type: String, label: String, maskedValue: String) = withContext(Dispatchers.IO) {
        dao.insertPaymentMethod(
            PaymentMethodEntity(type = type, label = label, maskedValue = maskedValue)
        )
    }

    suspend fun removePaymentMethod(id: Int) = withContext(Dispatchers.IO) {
        dao.deletePaymentMethod(id)
    }

    suspend fun setDefaultPaymentMethod(id: Int) = withContext(Dispatchers.IO) {
        dao.clearDefaultPaymentMethod()
        dao.setDefaultPaymentMethod(id)
    }

    // --- UPI Payment Simulation ---
    suspend fun simulateUpiPayment(upiId: String, amount: Double, otp: String): Boolean = withContext(Dispatchers.IO) {
        delay(2000) // Simulate network call
        if (otp == "123456") {
            // Success - add to wallet as a deposit record for tracking
            Log.d("AppRepository", "UPI Payment success: $upiId, amount=$amount")
            true
        } else {
            Log.d("AppRepository", "UPI Payment failed: wrong OTP")
            false
        }
    }

    // --- Card Payment Simulation ---
    suspend fun simulateCardPayment(cardLast4: String, amount: Double): Boolean = withContext(Dispatchers.IO) {
        delay(2500)
        // Accept test card 4242 or any 16-digit card
        val success = cardLast4.length == 4
        Log.d("AppRepository", "Card Payment ${if (success) "success" else "failed"}: ****$cardLast4, amount=$amount")
        success
    }

    // --- User Authentication ---
    suspend fun loginOrCreateUser(phone: String, name: String, email: String = "", role: String = "Customer"): UserEntity = withContext(Dispatchers.IO) {
        val existing = dao.getUserByPhone(phone)
        val token = java.util.UUID.randomUUID().toString()

        var remoteUser: com.example.data.api.MongoUser? = null
        try {
            val response = apiService.syncUserProfile(com.example.data.api.SyncUserRequest(phone, name, email.ifEmpty { existing?.email ?: "" }, role))
            if (response.isSuccessful && response.body()?.success == true) {
                remoteUser = response.body()?.data
                Log.d("AppRepository", "Synced profile with remote Supabase DB successfully: balance = ${remoteUser?.walletBalance}")
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to sync profile with remote Supabase: ${e.message}")
        }

        if (existing != null) {
            val updatedUser = existing.copy(
                sessionToken = token,
                name = remoteUser?.name ?: name,
                email = remoteUser?.email ?: email.ifEmpty { existing.email },
                walletBalance = remoteUser?.walletBalance ?: existing.walletBalance,
                lastLoginAt = System.currentTimeMillis(),
                role = remoteUser?.role ?: existing.role
            )
            dao.insertUser(updatedUser)
            
            if (remoteUser != null) {
                syncWalletTransactions(phone)
                syncAddressesFromRemote(phone)
            }
            updatedUser
        } else {
            val newUser = UserEntity(
                phone = phone,
                name = remoteUser?.name ?: name,
                email = remoteUser?.email ?: email,
                sessionToken = token,
                isVerified = true,
                walletBalance = remoteUser?.walletBalance ?: 100.0,
                createdAt = remoteUser?.createdAt ?: System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis(),
                role = remoteUser?.role ?: role
            )
            val id = dao.insertUser(newUser).toInt()
            val resolved = newUser.copy(id = id)
            
            if (remoteUser != null) {
                syncWalletTransactions(phone)
                syncAddressesFromRemote(phone)
            }
            resolved
        }
    }

    // --- Saved Addresses ---
    suspend fun saveAddress(userId: Int, label: String, fullAddress: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        val existingDefault = dao.getDefaultAddress(userId)
        val isFirst = existingDefault == null
        val addr = SavedAddressEntity(
            userId = userId,
            label = label,
            fullAddress = fullAddress,
            latitude = lat,
            longitude = lng,
            isDefault = isFirst
        )
        val newId = dao.insertSavedAddress(addr).toInt()

        val user = dao.getUserById(userId) ?: dao.getCurrentUser()
        if (user != null) {
            if (isFirst) {
                dao.updateDefaultAddress(user.phone, newId)
            }
            try {
                apiService.addAddress(
                    user.phone,
                    com.example.data.api.AddAddressRequest(
                        label = label,
                        fullAddress = fullAddress,
                        latitude = lat,
                        longitude = lng,
                        isDefault = isFirst
                    )
                )
                apiService.updateUserLocation(
                    com.example.data.api.LocationUpdateRequest(
                        userId = user.phone,
                        lat = lat,
                        lng = lng,
                        address = fullAddress
                    )
                )
                Log.d("AppRepository", "Synced address to remote Supabase database")
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to sync address to remote Supabase: ${e.message}")
            }
        }
    }

    suspend fun setDefaultAddress(userId: Int, addressId: Int) = withContext(Dispatchers.IO) {
        dao.clearDefaultAddress(userId)
        dao.setDefaultAddress(addressId)
        
        val user = dao.getUserById(userId) ?: dao.getCurrentUser()
        if (user != null) {
            dao.updateDefaultAddress(user.phone, addressId)
            
            try {
                // Fetch the list to find details for sync
                val list = dao.getSavedAddresses(userId).first()
                val addr = list.find { it.id == addressId }
                if (addr != null) {
                    apiService.updateUserLocation(
                        com.example.data.api.LocationUpdateRequest(
                            userId = user.phone,
                            lat = addr.latitude,
                            lng = addr.longitude,
                            address = addr.fullAddress
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to sync default address to remote database: ${e.message}")
            }
        }
    }

    suspend fun deleteAddress(addressId: Int) = withContext(Dispatchers.IO) {
        dao.deleteAddress(addressId)
    }

    // --- Sync Helpers ---
    private suspend fun syncWalletTransactions(phone: String) {
        try {
            val response = apiService.getWalletDetails(phone)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    val user = dao.getUserByPhone(phone)
                    if (user != null) {
                        dao.insertUser(user.copy(walletBalance = data.walletBalance))
                    }
                    dao.clearWalletTransactions()
                    data.transactions.forEach { tx ->
                        dao.insertWalletTransaction(
                            WalletTransactionEntity(
                                type = tx.type,
                                amount = tx.amount,
                                description = tx.description,
                                timestamp = tx.timestamp,
                                memberName = tx.memberName
                            )
                        )
                    }
                    Log.d("AppRepository", "Synchronized wallet transactions from remote Supabase DB")
                }
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to sync wallet transactions: ${e.message}")
        }
    }

    private suspend fun syncAddressesFromRemote(phone: String) {
        try {
            val response = apiService.getAddresses(phone)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    val user = dao.getUserByPhone(phone) ?: return
                    dao.clearSavedAddresses(user.id)
                    data.forEach { addr ->
                        val roomAddr = SavedAddressEntity(
                            userId = user.id,
                            label = addr.label,
                            fullAddress = addr.fullAddress,
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            isDefault = addr.isDefault
                        )
                        dao.insertSavedAddress(roomAddr)
                    }
                    Log.d("AppRepository", "Synchronized addresses from remote Supabase DB")
                }
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to sync addresses: ${e.message}")
        }
    }

    suspend fun getWalletBalance(): Double = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUser()
        if (user != null) {
            try {
                val response = apiService.getWalletDetails(user.phone)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        dao.clearWalletTransactions()
                        data.transactions.forEach { tx ->
                            dao.insertWalletTransaction(
                                WalletTransactionEntity(
                                    type = tx.type,
                                    amount = tx.amount,
                                    description = tx.description,
                                    timestamp = tx.timestamp,
                                    memberName = tx.memberName
                                )
                            )
                        }
                        dao.insertUser(user.copy(walletBalance = data.walletBalance))
                        return@withContext data.walletBalance
                    }
                }
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to get remote wallet balance, falling back to local cache: ${e.message}")
                return@withContext user.walletBalance
            }
        }
        100.0
    }

    suspend fun addWalletFunds(amount: Double) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUser()
        if (user != null) {
            try {
                val response = apiService.addWalletTransaction(
                    user.phone,
                    com.example.data.api.AddWalletTxRequest(
                        type = "Deposit",
                        amount = amount,
                        description = "Wallet fund deposit via UPI/Card"
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        dao.insertWalletTransaction(
                            WalletTransactionEntity(
                                type = data.transaction.type,
                                amount = data.transaction.amount,
                                description = data.transaction.description,
                                timestamp = data.transaction.timestamp,
                                memberName = data.transaction.memberName
                            )
                        )
                        dao.insertUser(user.copy(walletBalance = data.walletBalance))
                        return@withContext
                    }
                }
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to post remote wallet transaction: ${e.message}")
            }
        }
        // Local fallback for offline/API failure case
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                type = "Deposit",
                amount = amount,
                description = "Wallet fund deposit via UPI/Card",
                timestamp = System.currentTimeMillis()
            )
        )
        if (user != null) {
            dao.insertUser(user.copy(walletBalance = user.walletBalance + amount))
        }
    }

    suspend fun fetchRemoteOrders(
        userId: String? = null,
        restaurantId: String? = null,
        status: String? = null,
        unassigned: Boolean? = null
    ) {
        try {
            val response = apiService.getOrders(userId, restaurantId, status, unassigned)
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteList = response.body()?.data ?: emptyList()
                remoteList.forEach { remote ->
                    val existing = dao.getOrderByRemoteId(remote.id)
                    val restId = remote.restaurantId.toIntOrNull() ?: 1
                    val restName = dao.getRestaurantById(restId)?.name ?: "Restaurant #${remote.restaurantId}"
                    
                    val itemsSummaryBuilder = StringBuilder()
                    remote.items.forEachIndexed { index, item ->
                        if (index > 0) itemsSummaryBuilder.append(", ")
                        itemsSummaryBuilder.append("${item.quantity}x ${item.name}")
                    }
                    val itemsSummary = itemsSummaryBuilder.toString().ifEmpty { "Gourmet items" }
                    
                    val itemsDetailJson = try {
                        val array = org.json.JSONArray()
                        remote.items.forEach { item ->
                            val obj = org.json.JSONObject()
                            obj.put("dishId", item.dishId)
                            obj.put("name", item.name)
                            obj.put("quantity", item.quantity)
                            obj.put("price", item.price)
                            array.put(obj)
                        }
                        array.toString()
                    } catch (e: Exception) {
                        "[]"
                    }

                    if (existing != null) {
                        val statusChanged = existing.status != remote.status
                        dao.updateOrderDriverAndStatus(
                            existing.id,
                            remote.status,
                            remote.driverName,
                            remote.driverPhone,
                            remote.driverLat,
                            remote.driverLng
                        )
                        
                        val isRider = context.packageName.contains("rider")
                        if (isRider && statusChanged && remote.driverName.isEmpty() && (remote.status == "Placed" || remote.status == "Accepted" || remote.status == "Preparing")) {
                            com.example.utils.NotificationHelper.showNotification(
                                context,
                                "Gourmet Dispatch Update",
                                "Order at $restName is ${remote.status} and available!"
                            )
                            com.example.utils.NotificationHelper.playNotificationSound(context)
                        }
                    } else {
                        val newOrder = OrderEntity(
                            restaurantId = restId,
                            restaurantName = restName,
                            status = remote.status,
                            totalAmount = remote.totalAmount,
                            itemsSummary = itemsSummary,
                            itemsDetailJson = itemsDetailJson,
                            paymentMethod = remote.paymentMethod,
                            deliveryAddress = remote.deliveryAddress,
                            timestamp = remote.createdAt,
                            driverName = remote.driverName,
                            driverPhone = remote.driverPhone,
                            driverLat = remote.driverLat,
                            driverLng = remote.driverLng,
                            customerLat = remote.customerLat,
                            customerLng = remote.customerLng,
                            restaurantLat = remote.restaurantLat,
                            restaurantLng = remote.restaurantLng,
                            ratingGiven = remote.ratingGiven,
                            reviewText = remote.reviewText,
                            reviewSentiment = remote.reviewSentiment,
                            remoteId = remote.id
                        )
                        dao.insertOrder(newOrder)

                        // Trigger notifications with ring sound if running the Rider app module
                        val isRider = context.packageName.contains("rider")
                        if (isRider && remote.driverName.isEmpty() && (remote.status == "Placed" || remote.status == "Accepted" || remote.status == "Preparing")) {
                            com.example.utils.NotificationHelper.showNotification(
                                context,
                                "New Gourmet Dispatch",
                                "New order available at $restName!"
                            )
                            com.example.utils.NotificationHelper.playNotificationSound(context)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Error fetching remote orders: ${e.message}")
        }
    }

    suspend fun updateOrderRemoteStatus(
        orderId: String,
        localId: Int,
        status: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        try {
            val request = com.example.data.api.UpdateStatusRequest(status, lat, lng)
            val response = apiService.updateOrderStatus(orderId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = response.body()?.data
                if (updated != null) {
                    dao.updateOrderDriverAndStatus(
                        localId,
                        updated.status,
                        updated.driverName,
                        updated.driverPhone,
                        updated.driverLat,
                        updated.driverLng
                    )
                    return
                }
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to update status on remote: ${e.message}")
        }
        dao.updateOrderStatus(localId, status)
        if (lat != null && lng != null) {
            dao.updateDriverLocation(localId, lat, lng)
        }
    }

    suspend fun acceptRiderOnRemote(
        orderId: String,
        localId: Int,
        driverName: String,
        driverPhone: String,
        driverLat: Double,
        driverLng: Double
    ) {
        try {
            val request = com.example.data.api.AcceptRiderRequest(
                driverName = driverName,
                driverPhone = driverPhone,
                driverLat = driverLat,
                driverLng = driverLng
            )
            val response = apiService.acceptRider(orderId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = response.body()?.data
                if (updated != null) {
                    dao.updateOrderDriverAndStatus(
                        localId,
                        updated.status,
                        updated.driverName.ifEmpty { driverName },
                        updated.driverPhone.ifEmpty { driverPhone },
                        updated.driverLat,
                        updated.driverLng
                    )
                    return
                }
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to accept rider on remote: ${e.message}")
        }
        dao.updateOrderDriverAndStatus(localId, "Confirmed", driverName, driverPhone, driverLat, driverLng)
    }
}
