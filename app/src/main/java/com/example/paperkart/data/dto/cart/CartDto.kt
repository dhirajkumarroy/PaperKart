package com.example.paperkart.data.dto.cart

import com.example.paperkart.data.dto.product.ImageDto
import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("_id") val id: String,
    val user: String,
    val items: List<CartItemDto>
)

data class CartItemDto(
    // 🔥 Use a specific Product summary for Cart to avoid DTO mismatch crashes
    val product: CartProductSummaryDto,
    val sku: String,
    val name: String, // This is usually the product name
    val quantity: Int,
    val priceAtTime: Double,
    @SerializedName("_id") val itemId: String
)

data class CartProductSummaryDto(
    @SerializedName("_id") val id: String,
    val name: String,
    // 🔥 CRITICAL: Must be ImageDto to match our new Object format
    val coverImage: ImageDto?
)

data class AddToCartRequest(
    val productId: String,
    val sku: String,
    val quantity: Int = 1
)