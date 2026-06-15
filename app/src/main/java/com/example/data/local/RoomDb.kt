package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "restaurants", indices = [
    Index(value = ["cuisine"]),
    Index(value = ["rating"]),
    Index(value = ["isVeg"]),
    Index(value = ["isPromoted"])
])
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

@Entity(tableName = "dishes", indices = [
    Index(value = ["restaurantId"]),
    Index(value = ["category"]),
    Index(value = ["isBestseller"])
])
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

@Entity(tableName = "orders", indices = [
    Index(value = ["timestamp"]),
    Index(value = ["status"]),
    Index(value = ["restaurantId"])
])
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
    val customerLat: Double = 16.5062,
    val customerLng: Double = 80.6480,
    val restaurantLat: Double = 16.5062,
    val restaurantLng: Double = 80.6480,
    val ratingGiven: Float, // 0 if not rated
    val reviewText: String,
    val reviewSentiment: String // Neutral, Positive, Negative, Mixed
)

@Entity(tableName = "wallet_transactions", indices = [
    Index(value = ["timestamp"]),
    Index(value = ["type"])
])
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // Deposit, Payment, Cashback, Refund, FamilyDebit
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val memberName: String = "" // for family transactions - which member
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // User, Assistant
    val message: String,
    val timestamp: Long
)

// --- NEW: Family Member Entity ---
@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val avatarColor: Long = 0xFFFC8019, // hex color as Long
    val spendingLimit: Double = 100.0,   // monthly limit in $
    val monthlySpent: Double = 0.0,
    val isAdmin: Boolean = false,
    val isActive: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
)

// --- NEW: Family Transaction Entity ---
@Entity(tableName = "family_transactions")
data class FamilyTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val memberName: String,
    val orderId: Int,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- NEW: Payment Method Entity ---
@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,        // UPI, CARD
    val label: String,       // e.g. "tharun@paytm" or "Visa ****1234"
    val maskedValue: String, // e.g. "tharun@paytm" or "****1234"
    val isDefault: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

// --- User Profile Entity ---
@Entity(tableName = "users", indices = [
    Index(value = ["phone"], unique = true)
])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,           // Primary identifier (e.g. "919876543210")
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val sessionToken: String = "",
    val isVerified: Boolean = false,
    val defaultAddressId: Int = -1,
    val isGoldMember: Boolean = false,
    val walletBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

// --- Saved Delivery Address Entity ---
@Entity(tableName = "saved_addresses", indices = [
    Index(value = ["userId"]),
    Index(value = ["isDefault"])
])
data class SavedAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val label: String,           // "Home", "Work", "Other", or custom
    val fullAddress: String,     // Reverse-geocoded address string
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
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

    @Query("SELECT * FROM dishes")
    fun getAllDishes(): Flow<List<DishEntity>>

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

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearWalletTransactions()

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // --- Family Members ---
    @Query("SELECT * FROM family_members WHERE isActive = 1 ORDER BY isAdmin DESC, name ASC")
    fun getFamilyMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity): Long

    @Query("UPDATE family_members SET spendingLimit = :limit WHERE id = :id")
    suspend fun updateMemberSpendingLimit(id: Int, limit: Double)

    @Query("UPDATE family_members SET monthlySpent = monthlySpent + :amount WHERE id = :id")
    suspend fun incrementMemberSpending(id: Int, amount: Double)

    @Query("UPDATE family_members SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMember(id: Int)

    @Query("SELECT COUNT(*) FROM family_members WHERE isActive = 1")
    suspend fun getFamilyMemberCount(): Int

    @Query("SELECT SUM(monthlySpent) FROM family_members WHERE isActive = 1")
    suspend fun getTotalFamilySpending(): Double?

    // --- Family Transactions ---
    @Query("SELECT * FROM family_transactions ORDER BY timestamp DESC")
    fun getFamilyTransactions(): Flow<List<FamilyTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyTransaction(tx: FamilyTransactionEntity)

    @Query("SELECT * FROM family_transactions WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getTransactionsForMember(memberId: Int): Flow<List<FamilyTransactionEntity>>

    // --- Payment Methods ---
    @Query("SELECT * FROM payment_methods ORDER BY isDefault DESC, addedAt DESC")
    fun getPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deletePaymentMethod(id: Int)

    @Query("UPDATE payment_methods SET isDefault = 0")
    suspend fun clearDefaultPaymentMethod()

    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultPaymentMethod(id: Int)

    // --- Users ---
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET name = :name, email = :email WHERE phone = :phone")
    suspend fun updateUserProfile(phone: String, name: String, email: String)

    @Query("UPDATE users SET lastLoginAt = :ts, sessionToken = :token WHERE phone = :phone")
    suspend fun updateUserSession(phone: String, token: String, ts: Long)

    @Query("UPDATE users SET isVerified = 1 WHERE phone = :phone")
    suspend fun markUserVerified(phone: String)

    @Query("UPDATE users SET defaultAddressId = :addressId WHERE phone = :phone")
    suspend fun updateDefaultAddress(phone: String, addressId: Int)

    // --- Saved Addresses ---
    @Query("SELECT * FROM saved_addresses WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getSavedAddresses(userId: Int): Flow<List<SavedAddressEntity>>

    @Query("SELECT * FROM saved_addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(userId: Int): SavedAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAddress(address: SavedAddressEntity): Long

    @Query("UPDATE saved_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: Int)

    @Query("UPDATE saved_addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun setDefaultAddress(addressId: Int)

    @Query("DELETE FROM saved_addresses WHERE id = :id")
    suspend fun deleteAddress(id: Int)

    @Query("DELETE FROM saved_addresses WHERE userId = :userId")
    suspend fun clearSavedAddresses(userId: Int)
}

// --- App Database ---

@Database(
    entities = [
        RestaurantEntity::class,
        DishEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        ChatMessageEntity::class,
        FamilyMemberEntity::class,
        FamilyTransactionEntity::class,
        PaymentMethodEntity::class,
        UserEntity::class,
        SavedAddressEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PlatformDao
}
