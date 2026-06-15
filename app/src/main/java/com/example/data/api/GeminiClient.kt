package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-2.0-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder")
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local/mock responses.")
            return@withContext getMockResponse(prompt)
        }

        val apiKey = getApiKey()
        val url = "$BASE_URL?key=$apiKey"

        try {
            // Build direct JSON following the REST API specifications
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")
            
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstObj)
            }

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string()
                if (!response.isSuccessful || responseBodyStr == null) {
                    Log.e(TAG, "Request failed: ${response.code} - $responseBodyStr")
                    return@withContext getMockResponse(prompt)
                }

                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text part found")
                    }
                }
                return@withContext "I am unable to formulate a response at the moment."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateContent: ${e.message}", e)
            return@withContext getMockResponse(prompt)
        }
    }

    suspend fun analyzeSentiment(reviewText: String): String {
        val prompt = "Analyze the sentiment of this food and delivery review. Answer with ONLY one word: 'Positive', 'Negative', 'Neutral', or 'Mixed'. Review: \"$reviewText\""
        val systemInstruction = "You are a specialized fast sentiment analyzer. Return ONLY the single matching classification word."
        val result = generateContent(prompt, systemInstruction).trim()
        return when {
            result.contains("Positive", ignoreCase = true) -> "Positive"
            result.contains("Negative", ignoreCase = true) -> "Negative"
            result.contains("Mixed", ignoreCase = true) -> "Mixed"
            else -> "Neutral"
        }
    }

    suspend fun chatWithAiSupport(history: List<ChatMessageEntity>, newMessage: String): String {
        // Build chat context
        val context = StringBuilder()
        context.append("Current Cloud Kitchen Platform context:\n")
        context.append("- Platform Name: BiteCraft\n")
        context.append("- We support: Gourmet burgers, Artisan Pizzas, Asian Fusion noodles, Luxe Desserts\n")
        context.append("- System options: Refund claims, order cancel requests, membership (BiteCraft Gold), dynamic wallets, simulated real-time tracking.\n\n")
        context.append("Chat History:\n")
        history.takeLast(10).forEach {
            context.append("${it.sender}: ${it.message}\n")
        }
        context.append("User: $newMessage\n")
        context.append("Assistant:")

        val systemInstruction = "You are BiteCraft Customer Support Bot, a friendly, professional AI representative. Provide helpful answers, give refund details, support cancellations, and keep answers concise (under 3 sentences)."
        return generateContent(context.toString(), systemInstruction)
    }

    private fun getMockResponse(prompt: String): String {
        val promptClean = prompt.lowercase()
        return when {
            promptClean.contains("sentiment") -> {
                if (promptClean.contains("delight") || promptClean.contains("love") || promptClean.contains("good") || promptClean.contains("great") || promptClean.contains("best") || promptClean.contains("amazing")) "Positive"
                else if (promptClean.contains("bad") || promptClean.contains("cold") || promptClean.contains("late") || promptClean.contains("worst") || promptClean.contains("ruin")) "Negative"
                else "Neutral"
            }
            promptClean.contains("recommend") || promptClean.contains("suggest") || promptClean.contains("mood") -> {
                "🌟 **BiteCraft AI Gourmet Suggestions**:\n\nBased on your preference, here are top picks:\n1. **Truffle Truce Burger** from *The Gourmet Lab* (Rich earthy truffle aioli, smash patty)\n2. **Burrata Blush Pizza** from *Slice & Co.* (Fresh burrata, basil pesto oil)\n3. **Volcano Ramen bowl** from *Noodle Craft* (Spicy level: Customizable!)\n\nAdd a Luxe Dessert for the perfect meal! 🎉"
            }
            promptClean.contains("predict") || promptClean.contains("delivery time") || promptClean.contains("traffic") -> {
                "⏱️ **AI Predictive Delivery Time**: **23 minutes**\n\n- Kitchen Prep Time: 12 mins (optimized)\n- Distance factor: 2.4 km (+5 mins)\n- Routing forecast: Low traffic & clear skies."
            }
            // Support chat fallbacks
            promptClean.contains("cancel") -> {
                "Mock AI Customer Service: I understand you'd like to cancel. If your order is not yet marked as 'Accepted', you can cancel it directly in the tracking screen. Otherwise, I can issue a cancellation refund to your BiteCraft Wallet."
            }
            promptClean.contains("refund") -> {
                "Mock AI Customer Service: For eligible issues, refunds are processed instantly to your BiteCraft Wallet. Your wallet balance can be used on any future custom-order!"
            }
            promptClean.contains("gold") || promptClean.contains("member") -> {
                "Mock AI Customer Service: BiteCraft Gold offers unlimited free deliveries on orders above $12.00, and standard 10% cashbacks into your wallet on premium select restaurants!"
            }
            else -> {
                "Hello, I am the BiteCraft AI Assistant. I can help recommend the perfect dishes, answer questions about BiteCraft Gold membership benefits, refund options, or even check your order status."
            }
        }
    }
}
