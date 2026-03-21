package com.example.paperkart.features.auth

import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AuthApi
import com.example.paperkart.data.dto.auth.*
import com.example.paperkart.data.dto.user.UserDto

class AuthRepository(
    private val api: AuthApi,
    private val session: SessionManager
) {

    // ── 1. Login (Email/Password) ─────────────────────────────
    suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val res = api.login(LoginRequest(email, password))
            if (res.isSuccessful) {
                val data = res.body()?.data ?: return Result.failure(Exception("Invalid Response"))

                // 🛡️ ROLE GATE: Only allow 'USER'
                if (data.user.role?.uppercase() != "USER") {
                    return Result.failure(Exception("Access Denied: Admins must use the Web Portal."))
                }

                // ✅ SAVE SESSION: Tokens & Profile Info
                session.saveToken(data.accessToken)
                session.saveRefreshToken(data.refreshToken)
                session.saveUserData(data.user.name, data.user.email)

                Result.success(data.user)
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 2. Send OTP (Mobile Login) ────────────────────────────
    // This fixes your "Unresolved reference 'sendOtp'" error
    suspend fun sendOtp(phone: String): Result<String> {
        return try {
            val res = api.sendOtp(SendOtpRequest(normalizePhone(phone)))
            if (res.isSuccessful) {
                Result.success(res.body()?.message ?: "OTP Sent Successfully")
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 3. Verify OTP ─────────────────────────────────────────
    suspend fun verifyOtp(phone: String, otp: String): Result<UserDto> {
        return try {
            val res = api.verifyOtp(VerifyOtpRequest(normalizePhone(phone), otp))
            if (res.isSuccessful) {
                val data = res.body()?.data ?: return Result.failure(Exception("Invalid Response"))

                if (data.user.role?.uppercase() != "USER") {
                    return Result.failure(Exception("Access Denied: Admin account restricted on mobile."))
                }

                session.saveToken(data.accessToken)
                session.saveRefreshToken(data.refreshToken)
                session.saveUserData(data.user.name, data.user.email)

                Result.success(data.user)
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 4. Register ───────────────────────────────────────────
    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val res = api.register(RegisterRequest(name, email, password))
            if (res.isSuccessful) {
                Result.success(res.body()?.message ?: "Registration successful")
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 5. Forgot Password ────────────────────────────────────
    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val res = api.forgotPassword(ForgotPasswordRequest(email))
            if (res.isSuccessful) {
                Result.success("Reset link sent to your email")
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 6. Reset Password ─────────────────────────────────────
    suspend fun resetPassword(token: String, newPass: String): Result<String> {
        return try {
            val res = api.resetPassword(ResetPasswordRequest(token, newPass))
            if (res.isSuccessful) {
                Result.success("Password updated successfully")
            } else {
                Result.failure(Exception(parseError(res.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun normalizePhone(raw: String): String =
        if (raw.startsWith("+")) raw else "+91$raw"

    private fun parseError(body: String?): String {
        return try {
            // Regex to find "message":"..." in the JSON error string
            val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(body ?: "")?.groupValues?.get(1) ?: "An unexpected error occurred"
        } catch (e: Exception) {
            "An unexpected error occurred"
        }
    }
}