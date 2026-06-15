package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────────────────────────────────────
// Data Models matching your MongoDB Atlas "mydb" schema
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
    @Json(name = "_id")           val id: String = "",
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
    @Json(name = "createdAt")     val createdAt: Long = 0L
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
    @Json(name = "cached")  val cached: Boolean = false  // Redis cache hit indicator
)

@JsonClass(generateAdapter = true)
data class LocationUpdateRequest(
    @Json(name = "userId") val userId: String,
    @Json(name = "lat")    val lat: Double,
    @Json(name = "lng")    val lng: Double,
    @Json(name = "address") val address: String
)

// ─────────────────────────────────────────────────────────────────────────────
// Retrofit Service Interface
// Your Spring Boot backend → MongoDB Atlas + Redis
// Base URL configured in AppRepository / NetworkModule
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

    // ── Location update
    @PUT("api/users/location")
    suspend fun updateUserLocation(
        @Body request: LocationUpdateRequest
    ): Response<ApiResponse<Unit>>

    // ── Health check (also warms Redis)
    @GET("actuator/health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
