package com.example.paperkart.data.api

import com.example.paperkart.data.dto.product.ProductDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class ProductListResponse(
    val products: List<ProductDto>,
    val total: Int,
    val page: Int
)

interface ProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ProductListResponse> // ✅ FIXED

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): Response<ProductDto> // ✅ FIXED

    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Part coverImage: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>?,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("productType") productType: RequestBody,
        @Part("category") category: RequestBody,
        @Part("variants") variants: RequestBody,
        @Part("tags") tags: RequestBody?
    ): Response<ProductDto>
}