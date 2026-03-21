// ─────────────────────────────────────────────────────────────
// FILE: data/dto/auth/AuthRequests.kt
// ─────────────────────────────────────────────────────────────
package com.example.paperkart.data.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email:    String? = null,
    @SerializedName("phone")    val phone:    String? = null,
    @SerializedName("password") val password: String? = null
)

data class RegisterRequest(
    @SerializedName("name")     val name:     String,
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String
)

data class SendOtpRequest(
    @SerializedName("phone") val phone: String
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp")   val otp:   String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("token")    val token:    String,
    @SerializedName("password") val password: String
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class GoogleLoginRequest(
    @SerializedName("token") val token: String
)

data class SendPhoneLinkOtpRequest(
    @SerializedName("phone") val phone: String
)

data class VerifyPhoneLinkRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp")   val otp:   String
)

data class EmailLinkRequest(
    @SerializedName("email") val email: String
)