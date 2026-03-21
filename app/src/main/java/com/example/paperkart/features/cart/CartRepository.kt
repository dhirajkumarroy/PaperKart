package com.example.paperkart.features.cart

import com.example.paperkart.data.api.CartApi
import com.example.paperkart.data.dto.cart.AddToCartRequest

class CartRepository(private val api: CartApi) {

    suspend fun getCart() = api.getMyCart()

    suspend fun addToCart(productId: String, sku: String, quantity: Int) =
        api.addToCart(AddToCartRequest(productId, sku, quantity))

    suspend fun removeItem(productId: String, sku: String?) =
        api.removeFromCart(productId, sku)
}