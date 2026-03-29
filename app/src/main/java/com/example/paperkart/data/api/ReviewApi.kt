package com.example.paperkart.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    @GET("reviews/{productId}")
    suspend fun getProductReviews(
        @Path("productId") productId: String
    ): Response<List<ReviewDto>>

    @POST("reviews/{productId}")
    suspend fun addReview(
        @Path("productId") productId: String,
        @Body body: CreateReviewRequest
    ): Response<AddReviewResponse>
}

data class ReviewDto(
    @SerializedName("_id") val id: String,
    val user: ReviewUserDto,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class ReviewUserDto(
    val name: String
)

data class CreateReviewRequest(
    val rating: Int,
    val comment: String
)

data class AddReviewResponse(
    val message: String,
    val review: ReviewDto
)
