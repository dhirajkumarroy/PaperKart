package com.example.paperkart.data.api

import com.example.paperkart.data.dto.cart.AddToCartRequest
import com.example.paperkart.data.dto.cart.CartResponse
import retrofit2.Response
import retrofit2.http.*

interface CartApi {

    // ✅ REMOVED "api/" prefix to fix 404 double-path error
    @GET("cart")
    suspend fun getMyCart(): Response<CartResponse>

    // ✅ REMOVED "api/" prefix
    @POST("cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<CartResponse>

    // ✅ REMOVED "api/" prefix
    @DELETE("cart/{productId}")
    suspend fun removeFromCart(
        @Path("productId") productId: String,
        @Query("sku") sku: String? = null
    ): Response<CartResponse>
}