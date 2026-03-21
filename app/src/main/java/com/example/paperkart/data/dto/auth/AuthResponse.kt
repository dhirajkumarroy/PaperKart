// ─────────────────────────────────────────────────────────────
// FILE: data/dto/auth/AuthResponses.kt
// ─────────────────────────────────────────────────────────────
package com.example.paperkart.data.dto.auth

import com.example.paperkart.data.dto.user.UserDto
import com.google.gson.annotations.SerializedName

// Generic API envelope
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data")    val data:    T?
)

// POST /login  |  POST /send-otp  |  POST /verify-otp
data class AuthData(
    @SerializedName("accessToken")  val accessToken:  String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user")         val user:         UserDto
)

// GET /link/status
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

// Simple message response (logout, forgot-password, send-otp, etc.)
data class MessageData(
    @SerializedName("message") val message: String
)