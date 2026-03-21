package com.example.paperkart.data.api

import com.example.paperkart.data.dto.user.AddressDto
import com.example.paperkart.data.dto.user.UserDto
import com.example.paperkart.data.dto.user.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    /* =========================
       PROFILE
    ========================= */

    @GET("users/profile")
    suspend fun getProfile(): Response<UserResponse>

    // Matches router.put("/profile", updateProfile)
    @PUT("users/profile")
    suspend fun updateProfile(
        @Body user: UserDto
    ): Response<UserResponse>

    /* =========================
       ADDRESS
    ========================= */

    // Matches router.get("/saved-address", getSavedAddress)
    @GET("users/saved-address")
    suspend fun getSavedAddress(): Response<UserResponse>

    // Matches router.put("/saved-address", saveAddress)
    @PUT("users/saved-address")
    suspend fun saveAddress(
        @Body address: AddressDto
    ): Response<UserResponse>

    // Matches router.delete("/saved-address", deleteAddress)
    @DELETE("users/saved-address")
    suspend fun deleteAddress(): Response<UserResponse>
}