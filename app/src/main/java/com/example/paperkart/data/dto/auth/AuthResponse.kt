package com.example.paperkart.data.dto.auth

import com.example.paperkart.data.dto.user.UserDto
import com.google.gson.annotations.SerializedName

/**
 * ── UNIVERSAL ENVELOPE ──
 * Matches your Node.js success() helper: { success, message, data }
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data")    val data:    T?
)

/**
 * ── AUTH DATA (Login, Verify OTP, Refresh Token) ──
 * Used when the backend returns a new session.
 */
data class AuthData(
    @SerializedName("accessToken")  val accessToken:  String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user")         val user:         UserDto
)

/**
 * ── LINK STATUS ──
 * Used for post-login logic to check if user needs to link phone/email.
 */
data class LinkStatusData(
    @SerializedName("providers")       val providers:       List<String>,
    @SerializedName("hasEmail")        val hasEmail:        Boolean,
    @SerializedName("hasPhone")        val hasPhone:        Boolean,
    @SerializedName("hasName")         val hasName:         Boolean,
    @SerializedName("emailVerified")   val emailVerified:   Boolean,
    @SerializedName("shouldLinkPhone") val shouldLinkPhone: Boolean,
    @SerializedName("shouldLinkEmail") val shouldLinkEmail: Boolean,
    @SerializedName("shouldAddName")   val shouldAddName:   Boolean
)

/**
 * ── MESSAGE ONLY ──
 * For simple actions like logout, forgot-password, or change-password.
 */
data class MessageData(
    @SerializedName("message") val message: String
)