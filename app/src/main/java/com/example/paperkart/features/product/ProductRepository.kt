package com.example.paperkart.features.product

import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.data.api.ProductListResponse


class ProductRepository(private val api: ProductApi) {

    suspend fun getProducts(): ProductListResponse? {
        val res = api.getProducts()

        return if (res.isSuccessful) {
            res.body() // ✅ NO .data
        } else null
    }

    suspend fun getProductById(id: String) =
        api.getProductById(id).body() // ✅ NO .data
}