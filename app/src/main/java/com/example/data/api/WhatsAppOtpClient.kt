package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Simple test-mode OTP client.
 * Generates a 6-digit OTP, stores it in-memory, and always returns
 * the OTP in debugOtp so the login screen can show it on-screen.
 * No network calls are made — perfect for development / demo.
 */
object WhatsAppOtpClient {
    private const val TAG = "OtpTestMode"

    // In-memory OTP store: phone -> (otp, expiryMs)
    private val pendingOtps = mutableMapOf<String, Pair<String, Long>>()
    private const val OTP_TTL_MS = 5 * 60 * 1000L // 5 minutes

    /**
     * Generate a 6-digit OTP for the phone number and return it as debugOtp.
     * The OTP is shown directly in the login UI — no SMS/WhatsApp needed.
     */
    suspend fun sendOtp(phone: String): SendOtpResult = withContext(Dispatchers.IO) {
        val otp = String.format("%06d", Random.nextInt(100000, 999999))
        val expiry = System.currentTimeMillis() + OTP_TTL_MS
        pendingOtps[phone] = Pair(otp, expiry)

        Log.d(TAG, "Test OTP for $phone: $otp")

        SendOtpResult(
            success = true,
            otpSent = false,      // false = test mode, so debugOtp is always shown
            debugOtp = otp        // always expose — shown in the yellow banner on LoginScreen
        )
    }

    /**
     * Verify the OTP entered by the user against the stored value.
     */
    fun verifyOtp(phone: String, enteredOtp: String): VerifyResult {
        val stored = pendingOtps[phone]
            ?: return VerifyResult(false, "No OTP requested for this number")
        val (storedOtp, expiry) = stored

        if (System.currentTimeMillis() > expiry) {
            pendingOtps.remove(phone)
            return VerifyResult(false, "OTP expired. Please request a new one.")
        }

        return if (enteredOtp.trim() == storedOtp) {
            pendingOtps.remove(phone)
            VerifyResult(true, "Verified successfully")
        } else {
            VerifyResult(false, "Incorrect OTP. Please try again.")
        }
    }

    fun clearOtp(phone: String) {
        pendingOtps.remove(phone)
    }

    data class SendOtpResult(
        val success: Boolean,
        val otpSent: Boolean,        // always false in test mode
        val debugOtp: String? = null // shown in the yellow banner on LoginScreen
    )

    data class VerifyResult(
        val verified: Boolean,
        val message: String
    )
}
