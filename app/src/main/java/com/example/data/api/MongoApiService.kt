package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────────────────────────────────────
// Data Models matching your Supabase PostgreSQL schemas
// ─────────────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class MongoRestaurant(
    @Json(name = "_id")       val id: String = "",
    @Json(name = "name")      val name: String = "",
    @Json(name = "cuisine")   val cuisine: String = "",
    @Json(name = "rating")    val rating: Float = 0f,
    @Json(name = "deliveryTime") val deliveryTime: Int = 30,
    @Json(name = "deliveryFee")  val deliveryFee: Double = 0.0,
    @Json(name = "imageUrl")  val imageUrl: String = "",
    @Json(name = "address")   val address: String = "",
    @Json(name = "latitude")  val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "isVeg")     val isVeg: Boolean = false,
    @Json(name = "isPromoted") val isPromoted: Boolean = false,
    @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class MongoOrder(
    @Json(name = "id")            val id: String = "",
    @Json(name = "userId")        val userId: String = "",
    @Json(name = "restaurantId")  val restaurantId: String = "",
    @Json(name = "items")         val items: List<MongoOrderItem> = emptyList(),
    @Json(name = "totalAmount")   val totalAmount: Double = 0.0,
    @Json(name = "status")        val status: String = "Placed",
    @Json(name = "deliveryAddress") val deliveryAddress: String = "",
    @Json(name = "paymentMethod") val paymentMethod: String = "",
    @Json(name = "driverLat")     val driverLat: Double = 0.0,
    @Json(name = "driverLng")     val driverLng: Double = 0.0,
    @Json(name = "customerLat")   val customerLat: Double = 0.0,
    @Json(name = "customerLng")   val customerLng: Double = 0.0,
    @Json(name = "restaurantLat") val restaurantLat: Double = 0.0,
    @Json(name = "restaurantLng") val restaurantLng: Double = 0.0,
    @Json(name = "createdAt")     val createdAt: Long = 0L,
    @Json(name = "ratingGiven")   val ratingGiven: Float = 0.0f,
    @Json(name = "reviewText")    val reviewText: String = "",
    @Json(name = "reviewSentiment") val reviewSentiment: String = "Neutral"
)

@JsonClass(generateAdapter = true)
data class MongoOrderItem(
    @Json(name = "dishId")   val dishId: String = "",
    @Json(name = "name")     val name: String = "",
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "price")    val price: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PlaceOrderRequest(
    @Json(name = "userId")          val userId: String? = null,
    @Json(name = "restaurantId")    val restaurantId: String,
    @Json(name = "items")           val items: List<MongoOrderItem>,
    @Json(name = "totalAmount")     val totalAmount: Double,
    @Json(name = "deliveryAddress") val deliveryAddress: String,
    @Json(name = "paymentMethod")   val paymentMethod: String,
    @Json(name = "customerLat")     val customerLat: Double,
    @Json(name = "customerLng")     val customerLng: Double,
    @Json(name = "restaurantLat")   val restaurantLat: Double,
    @Json(name = "restaurantLng")   val restaurantLng: Double
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data")    val data: T? = null,
    @Json(name = "message") val message: String = "",
    @Json(name = "cached")  val cached: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LocationUpdateRequest(
    @Json(name = "userId") val userId: String,
    @Json(name = "lat")    val lat: Double,
    @Json(name = "lng")    val lng: Double,
    @Json(name = "address") val address: String
)

@JsonClass(generateAdapter = true)
data class SyncUserRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "name")  val name: String,
    @Json(name = "email") val email: String = ""
)

@JsonClass(generateAdapter = true)
data class MongoUser(
    @Json(name = "phone")            val phone: String,
    @Json(name = "name")             val name: String = "",
    @Json(name = "email")            val email: String = "",
    @Json(name = "avatarUrl")        val avatarUrl: String = "",
    @Json(name = "sessionToken")     val sessionToken: String = "",
    @Json(name = "isVerified")       val isVerified: Boolean = false,
    @Json(name = "defaultAddressId") val defaultAddressId: Int = -1,
    @Json(name = "isGoldMember")     val isGoldMember: Boolean = false,
    @Json(name = "walletBalance")    val walletBalance: Double = 0.0,
    @Json(name = "createdAt")        val createdAt: Long = 0L,
    @Json(name = "lastLoginAt")      val lastLoginAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class MongoWalletTransaction(
    @Json(name = "type")        val type: String,
    @Json(name = "amount")      val amount: Double,
    @Json(name = "description") val description: String,
    @Json(name = "timestamp")   val timestamp: Long,
    @Json(name = "memberName")  val memberName: String = ""
)

@JsonClass(generateAdapter = true)
data class WalletSyncResponse(
    @Json(name = "walletBalance") val walletBalance: Double,
    @Json(name = "transactions")  val transactions: List<MongoWalletTransaction>
)

@JsonClass(generateAdapter = true)
data class AddWalletTxRequest(
    @Json(name = "type")        val type: String,
    @Json(name = "amount")      val amount: Double,
    @Json(name = "description") val description: String,
    @Json(name = "memberName")  val memberName: String = ""
)

@JsonClass(generateAdapter = true)
data class MongoAddress(
    @Json(name = "label")       val label: String,
    @Json(name = "fullAddress") val fullAddress: String,
    @Json(name = "latitude")    val latitude: Double,
    @Json(name = "longitude")   val longitude: Double,
    @Json(name = "isDefault")   val isDefault: Boolean,
    @Json(name = "createdAt")   val createdAt: Long
)

@JsonClass(generateAdapter = true)
data class AddAddressRequest(
    @Json(name = "label")       val label: String,
    @Json(name = "fullAddress") val fullAddress: String,
    @Json(name = "latitude")    val latitude: Double,
    @Json(name = "longitude")   val longitude: Double,
    @Json(name = "isDefault")   val isDefault: Boolean
)

@JsonClass(generateAdapter = true)
data class SubmitReviewRequest(
    @Json(name = "rating")          val rating: Float,
    @Json(name = "reviewText")     val reviewText: String,
    @Json(name = "sentiment")      val sentiment: String
)

// ─────────────────────────────────────────────────────────────────────────────
// Retrofit Service Interface
// ─────────────────────────────────────────────────────────────────────────────
interface MongoApiService {

    // ── Restaurants (Redis cached on backend, 10min TTL)
    @GET("api/restaurants")
    suspend fun getRestaurants(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("cuisine") cuisine: String? = null
    ): Response<ApiResponse<List<MongoRestaurant>>>

    @GET("api/restaurants/{id}")
    suspend fun getRestaurantById(
        @Path("id") restaurantId: String
    ): Response<ApiResponse<MongoRestaurant>>

    // ── Orders
    @GET("api/orders")
    suspend fun getUserOrders(
        @Query("userId") userId: String
    ): Response<ApiResponse<List<MongoOrder>>>

    @POST("api/orders")
    suspend fun placeOrder(
        @Body request: PlaceOrderRequest
    ): Response<ApiResponse<MongoOrder>>

    @GET("api/orders/{id}/track")
    suspend fun trackOrder(
        @Path("id") orderId: String
    ): Response<ApiResponse<MongoOrder>>

    @POST("api/orders/{id}/review")
    suspend fun submitOrderReview(
        @Path("id") orderId: String,
        @Body request: SubmitReviewRequest
    ): Response<ApiResponse<MongoOrder>>

    // ── User location and profile sync
    @POST("api/users/profile")
    suspend fun syncUserProfile(
        @Body request: SyncUserRequest
    ): Response<ApiResponse<MongoUser>>

    @PUT("api/users/location")
    suspend fun updateUserLocation(
        @Body request: LocationUpdateRequest
    ): Response<ApiResponse<Unit>>

    // ── Wallet Sync
    @GET("api/users/{phone}/wallet")
    suspend fun getWalletDetails(
        @Path("phone") phone: String
    ): Response<ApiResponse<WalletSyncResponse>>

    @POST("api/users/{phone}/wallet")
    suspend fun addWalletTransaction(
        @Path("phone") phone: String,
        @Body request: AddWalletTxRequest
    ): Response<ApiResponse<WalletSyncResponse>>

    // ── Address Sync
    @GET("api/users/{phone}/addresses")
    suspend fun getAddresses(
        @Path("phone") phone: String
    ): Response<ApiResponse<List<MongoAddress>>>

    @POST("api/users/{phone}/addresses")
    suspend fun addAddress(
        @Path("phone") phone: String,
        @Body request: AddAddressRequest
    ): Response<ApiResponse<MongoAddress>>

    @DELETE("api/users/{phone}/addresses/{id}")
    suspend fun deleteAddress(
        @Path("phone") phone: String,
        @Path("id") addressId: String
    ): Response<ApiResponse<Unit>>

    // ── Health check
    @GET("actuator/health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
