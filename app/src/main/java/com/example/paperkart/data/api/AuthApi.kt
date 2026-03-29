// ─────────────────────────────────────────────────────────────
// FILE: data/api/AuthApi.kt
// ─────────────────────────────────────────────────────────────
package com.example.paperkart.data.api

import com.example.paperkart.data.dto.auth.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    /* ── Public ───────────────────────────────────────────── */

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @GET("auth/verify-email/{token}")
    suspend fun verifyEmail(
        @Path("token") token: String
    ): Response<ApiResponse<MessageData>>

    @POST("auth/google")
    suspend fun googleLogin(
        @Body body: GoogleLoginRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body body: SendOtpRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body body: VerifyOtpRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequest
    ): Response<ApiResponse<MessageData>>

    /* ── Protected (requires Bearer token) ───────────────── */

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<MessageData>>

    @POST("auth/change-password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/link/phone/send-otp")
    suspend fun sendPhoneLinkOtp(
        @Body body: SendPhoneLinkOtpRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/link/phone/verify")
    suspend fun verifyAndLinkPhone(
        @Body body: VerifyPhoneLinkRequest
    ): Response<ApiResponse<MessageData>>

    @POST("auth/link/email")
    suspend fun sendEmailLink(
        @Body body: EmailLinkRequest
    ): Response<ApiResponse<MessageData>>

    @GET("auth/link/status")
    suspend fun getLinkStatus(): Response<ApiResponse<LinkStatusData>>

    // Inside AuthApi.kt
    @POST("auth/refresh-token")
    suspend fun refreshAccessToken(
        @Body body: RefreshRequest  // ✅ Change this to @Body RefreshRequest
    ): retrofit2.Response<ApiResponse<AuthData>>
}