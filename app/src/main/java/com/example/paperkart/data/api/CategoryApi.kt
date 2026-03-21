package com.example.paperkart.data.api

import com.example.paperkart.data.dto.category.CategoryDto
import com.example.paperkart.data.dto.product.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CategoryApi {

    /**
     * Fetches the full category tree.
     * Path updated to remove 'api/' prefix to fix 404 double-path error.
     */
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    /**
     * Fetches products belonging to a specific category.
     * Path updated to match your backend structure: /api/products/category/{id}
     */
    @GET("products/category/{categoryId}")
    suspend fun getProductsByCategory(
        @Path("categoryId") categoryId: String
    ): Response<List<ProductDto>>
}