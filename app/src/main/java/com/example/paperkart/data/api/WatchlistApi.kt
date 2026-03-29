package com.example.paperkart.data.api

import com.example.paperkart.data.dto.product.ProductDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface WatchlistApi {

    @GET("watchlist")
    suspend fun getWatchlist(): Response<List<WatchlistItemDto>>

    @GET("watchlist/check/{productId}")
    suspend fun checkWatchlist(
        @Path("productId") productId: String
    ): Response<WatchlistCheckResponse>

    @POST("watchlist")
    suspend fun addToWatchlist(
        @Body body: Map<String, String>
    ): Response<WatchlistItemDto>

    @DELETE("watchlist/{productId}")
    suspend fun removeFromWatchlist(
        @Path("productId") productId: String
    ): Response<Unit>
}

data class WatchlistItemDto(
    @SerializedName("_id") val id: String,
    val product: ProductDto,
    val createdAt: String
)

data class WatchlistCheckResponse(
    val watched: Boolean
)
