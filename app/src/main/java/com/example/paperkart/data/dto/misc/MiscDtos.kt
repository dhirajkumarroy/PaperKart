// FILE: data/dto/misc/MiscDtos.kt
package com.example.paperkart.data.dto.misc

import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════════════════════
// NOTIFICATION  (matches Notification model exactly)
// ══════════════════════════════════════════════════════════════

data class NotificationDto(
    @SerializedName("_id")       val id:        String,
    @SerializedName("title")     val title:     String,
    @SerializedName("message")   val message:   String,
    @SerializedName("type")      val type:      String,   // ORDER | PAYMENT | SYSTEM | PROMOTION
    @SerializedName("read")      val read:      Boolean,
    @SerializedName("link")      val link:      String?,
    @SerializedName("createdAt") val createdAt: String
)

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data:    NotificationPageData?
)

data class NotificationPageData(
    @SerializedName("notifications") val notifications: List<NotificationDto>,
    @SerializedName("total")         val total:         Int?,
    @SerializedName("unread")        val unread:        Int?
)

data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("unread")  val unread:  Int
)

// ══════════════════════════════════════════════════════════════
// REVIEW  (matches Review model)
// ══════════════════════════════════════════════════════════════

data class ReviewUserDto(
    @SerializedName("_id")  val id:   String?,
    @SerializedName("name") val name: String?
)

data class ReviewDto(
    @SerializedName("_id")       val id:        String,
    @SerializedName("user")      val user:      ReviewUserDto?,
    @SerializedName("rating")    val rating:    Float,
    @SerializedName("comment")   val comment:   String,
    @SerializedName("createdAt") val createdAt: String
)

data class AddReviewRequest(
    @SerializedName("rating")  val rating:  Float,
    @SerializedName("comment") val comment: String
)

// ══════════════════════════════════════════════════════════════
// WATCHLIST  (matches Watchlist model)
// ══════════════════════════════════════════════════════════════

data class WatchlistItemDto(
    @SerializedName("_id")       val id:        String,
    @SerializedName("book")      val product:   WatchlistProductDto?,
    @SerializedName("createdAt") val createdAt: String
)

data class WatchlistProductDto(
    @SerializedName("_id")        val id:         String,
    @SerializedName("name")       val name:       String,
    @SerializedName("coverImage") val coverImage: String,
    @SerializedName("variants")   val variants:   List<WatchlistVariantDto>?
) {
    val minPrice: Double get() = variants?.minOfOrNull { it.price } ?: 0.0
}

data class WatchlistVariantDto(
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Int
)

// ══════════════════════════════════════════════════════════════
// CART  (client-side only — Android manages cart locally)
// ══════════════════════════════════════════════════════════════

data class CartItemDto(
    val productId:  String,
    val productName: String,
    val coverImage: String,
    val sku:        String,
    val attributes: Map<String, String>?,
    val price:      Double,
    val quantity:   Int,
    val maxStock:   Int
) {
    val subtotal: Double get() = price * quantity
}