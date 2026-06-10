package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val cuisine: String,
    val rating: Float,
    val deliveryTime: Int, // minutes
    val deliveryFee: Double,
    val image: String,
    val bannerImage: String,
    val address: String,
    val isVeg: Boolean,
    val isPromoted: Boolean,
    val distanceKm: Float,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: Int,
    val restaurantId: Int,
    val name: String,
    val price: Double,
    val description: String,
    val image: String,
    val category: String,
    val isVeg: Boolean,
    val isBestseller: Boolean,
    val spiceLevelSupport: Boolean,
    val addonsJson: String // Serialized list of addon options
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dishId: Int,
    val restaurantId: Int,
    val restaurantName: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val spiceLevel: String,
    val addons: String,
    val notes: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val restaurantName: String,
    val status: String, // Placed, Accepted, Preparing, OutForDelivery, Delivered, Cancelled
    val totalAmount: Double,
    val itemsSummary: String,
    val itemsDetailJson: String,
    val paymentMethod: String,
    val deliveryAddress: String,
    val timestamp: Long,
    val driverName: String,
    val driverPhone: String,
    val driverLat: Double,
    val driverLng: Double,
    val customerLat: Double = 12.935123,
    val customerLng: Double = 77.624128,
    val restaurantLat: Double = 12.9715987,
    val restaurantLng: Double = 77.5945627,
    val ratingGiven: Float, // 0 if not rated
    val reviewText: String,
    val reviewSentiment: String // Neutral, Positive, Negative, Mixed
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // Deposit, Payment, Cashback, Refund
    val amount: Double,
    val description: String,
    val timestamp: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // User, Assistant
    val message: String,
    val timestamp: Long
)

// --- DAOs ---

@Dao
interface PlatformDao {
    // Restaurants
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantById(id: Int): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Query("DELETE FROM restaurants")
    suspend fun clearRestaurants()

    // Dishes
    @Query("SELECT * FROM dishes WHERE restaurantId = :restaurantId")
    fun getDishesForRestaurant(restaurantId: Int): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id = :id LIMIT 1")
    suspend fun getDishById(id: Int): DishEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<DishEntity>)

    @Query("DELETE FROM dishes WHERE restaurantId = :restaurantId")
    suspend fun clearDishesForRestaurant(restaurantId: Int)

    // Cart Items
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderFlowById(id: Int): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE orders SET driverLat = :lat, driverLng = :lng WHERE id = :id")
    suspend fun updateDriverLocation(id: Int, lat: Double, lng: Double)

    @Query("UPDATE orders SET ratingGiven = :rating, reviewText = :review, reviewSentiment = :sentiment WHERE id = :id")
    suspend fun submitOrderReview(id: Int, rating: Float, review: String, sentiment: String)

    // Wallet transaction and calculations
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getWalletTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(tx: WalletTransactionEntity)

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}

// --- App Database ---

@Database(
    entities = [
        RestaurantEntity::class,
        DishEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PlatformDao
}
