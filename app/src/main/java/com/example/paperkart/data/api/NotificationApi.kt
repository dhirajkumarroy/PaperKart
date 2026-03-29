package com.example.paperkart.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {

    @GET("notifications")
    suspend fun getMyNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotificationListResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<NotificationDto>

    @PATCH("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>
}

data class NotificationListResponse(
    val success: Boolean,
    val data: List<NotificationDto>
)

data class NotificationDto(
    @SerializedName("_id") val id: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String,
    val data: Map<String, String>?
)

data class UnreadCountResponse(
    val success: Boolean,
    val unread: Int
)
