package com.example.paperkart.features.auth

import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AppApi
import com.example.paperkart.data.dto.auth.*

class AuthRepository(
    private val api: AppApi,
    private val session: SessionManager
) {

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val res = api.register(RegisterRequest(name, email, password))
            if (res.isSuccessful) {
                Result.success(res.body()?.message ?: "Registration successful")
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun login(email: String, password: String): Result<AuthData> {
        return try {
            val res = api.login(LoginRequest(email = email, password = password))
            if (res.isSuccessful) {
                val data = res.body()?.data ?: return Result.failure(Exception("Invalid response"))
                session.saveToken(data.accessToken)
                session.saveRefreshToken(data.refreshToken)
                Result.success(data)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendOtp(phone: String): Result<String> {
        return try {
            val res = api.sendOtp(SendOtpRequest(phone))
            if (res.isSuccessful) Result.success("OTP sent")
            else Result.failure(Exception("Failed to send OTP"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<AuthData> {
        return try {
            val res = api.verifyOtp(VerifyOtpRequest(phone, otp))
            if (res.isSuccessful) {
                val data = res.body()?.data ?: return Result.failure(Exception("Invalid response"))
                session.saveToken(data.accessToken)
                session.saveRefreshToken(data.refreshToken)
                Result.success(data)
            } else {
                Result.failure(Exception("Verification failed"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            session.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            session.clearSession()
            Result.success(Unit)
        }
    }
}
