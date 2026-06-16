package com.example.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.WalletTransactionEntity
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OrderSimulationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getInt("order_id", -1)
        if (orderId == -1) {
            Log.e("OrderSimulationWorker", "No order ID specified")
            return Result.failure()
        }

        Log.d("OrderSimulationWorker", "Starting order status simulation for ID: $orderId")
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bitecraft_database.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        val dao = db.dao()

        try {
            val order = dao.getOrderById(orderId)
            if (order == null) {
                Log.e("OrderSimulationWorker", "Order not found in DB")
                db.close()
                return Result.failure()
            }

            val currentStatus = order.status
            Log.d("OrderSimulationWorker", "Simulating starting from status: $currentStatus")

            if (currentStatus == "Placed") {
                delay(4000)
                dao.updateOrderStatus(orderId, "Accepted")
                NotificationHelper.showNotification(applicationContext, "Order Update", "Your order has been Accepted by the restaurant!")
            }

            if (currentStatus == "Placed" || currentStatus == "Accepted") {
                delay(5000)
                dao.updateOrderStatus(orderId, "Preparing")
                NotificationHelper.showNotification(applicationContext, "Order Update", "The chef is preparing your delicious meal!")
            }

            if (currentStatus == "Placed" || currentStatus == "Accepted" || currentStatus == "Preparing") {
                delay(7000)
                dao.updateOrderStatus(orderId, "OutForDelivery")
                NotificationHelper.showNotification(applicationContext, "Order Update", "Your order is Out for Delivery! Keep track of the rider on the map.")
            }

            // Driver coordinates simulation
            val freshOrder = dao.getOrderById(orderId)
            if (freshOrder != null) {
                val startLat = if (freshOrder.status == "OutForDelivery" && freshOrder.driverLat != 0.0) freshOrder.driverLat else freshOrder.restaurantLat
                val startLng = if (freshOrder.status == "OutForDelivery" && freshOrder.driverLng != 0.0) freshOrder.driverLng else freshOrder.restaurantLng
                val endLat = freshOrder.customerLat
                val endLng = freshOrder.customerLng

                val osrmRoutePoints = try {
                    val urlStr = "https://router.project-osrm.org/route/v1/driving/$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson"
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    conn.requestMethod = "GET"
                    val responseCode = conn.responseCode
                    if (responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        val routes = json.optJSONArray("routes")
                        val geometry = routes?.optJSONObject(0)?.optJSONObject("geometry")
                        val coords = geometry?.optJSONArray("coordinates")
                        val points = mutableListOf<Pair<Double, Double>>()
                        if (coords != null) {
                            for (idx in 0 until coords.length()) {
                                val pair = coords.getJSONArray(idx)
                                points.add(Pair(pair.getDouble(1), pair.getDouble(0))) // (lat, lng)
                            }
                        }
                        points
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("OrderSimulationWorker", "Failed to fetch OSRM route: ${e.message}")
                    emptyList()
                }

                if (osrmRoutePoints.isNotEmpty()) {
                    val maxSteps = 15
                    val stepSize = Math.max(1, osrmRoutePoints.size / maxSteps)
                    val sampledPoints = mutableListOf<Pair<Double, Double>>()
                    for (idx in 0 until osrmRoutePoints.size step stepSize) {
                        sampledPoints.add(osrmRoutePoints[idx])
                    }
                    if (sampledPoints.lastOrNull() != osrmRoutePoints.last()) {
                        sampledPoints.add(osrmRoutePoints.last())
                    }
                    for (point in sampledPoints) {
                        delay(2500)
                        dao.updateDriverLocation(orderId, point.first, point.second)
                    }
                } else {
                    val steps = 15
                    for (i in 1..steps) {
                        val fraction = i.toDouble() / steps
                        val currentLat = startLat + (endLat - startLat) * fraction
                        val currentLng = startLng + (endLng - startLng) * fraction
                        delay(2500)
                        dao.updateDriverLocation(orderId, currentLat, currentLng)
                    }
                }
            }

            // 4. Delivered
            dao.updateOrderStatus(orderId, "Delivered")
            NotificationHelper.showNotification(applicationContext, "Order Update", "Delivered! Enjoy your meal 🍽️")

            // Insert cashback transaction if applicable
            val currentOrder = dao.getOrderById(orderId)
            if (currentOrder != null) {
                val user = dao.getCurrentUser()
                if (user != null && user.isGoldMember) {
                    val cashback = currentOrder.totalAmount * 0.10 // 10% Gold cashback
                    dao.insertWalletTransaction(
                        WalletTransactionEntity(
                            type = "Cashback",
                            amount = cashback,
                            description = "10% Gold Cashback for Order #${orderId}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    // Update user's wallet balance locally
                    val updatedBal = user.walletBalance + cashback
                    dao.insertUser(user.copy(walletBalance = updatedBal))
                }
            }

        } catch (e: Exception) {
            Log.e("OrderSimulationWorker", "Error in simulation", e)
            return Result.failure()
        } finally {
            db.close()
        }

        return Result.success()
    }
}
