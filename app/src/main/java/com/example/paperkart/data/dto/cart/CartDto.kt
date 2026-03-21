package com.example.paperkart.data.dto.cart

import com.example.paperkart.data.dto.product.ProductDto

data class CartResponse(
    val _id: String,
    val user: String,
    val items: List<CartItemDto>
)

data class CartItemDto(
    val product: ProductDto, // Populated from backend
    val sku: String,
    val name: String,
    val quantity: Int,
    val priceAtTime: Double,
    val _id: String
)

data class AddToCartRequest(
    val productId: String,
    val sku: String,
    val quantity: Int = 1
)