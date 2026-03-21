// FILE: data/dto/order/OrderDtos.kt
package com.example.paperkart.data.dto.order

import com.google.gson.annotations.SerializedName

// ── Request ───────────────────────────────────────────────────

data class OrderItemRequest(
    @SerializedName("product")  val product:  String,   // Product _id
    @SerializedName("sku")      val sku:      String,
    @SerializedName("quantity") val quantity: Int
)

data class ShippingAddressRequest(
    @SerializedName("fullName")    val fullName:    String,
    @SerializedName("phone")       val phone:       String,
    @SerializedName("addressLine") val addressLine: String,
    @SerializedName("city")        val city:        String,
    @SerializedName("state")       val state:       String,
    @SerializedName("pincode")     val pincode:     String,
    @SerializedName("landmark")    val landmark:    String? = null
)

data class PlaceOrderRequest(
    @SerializedName("items")           val items:           List<OrderItemRequest>,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddressRequest,
    @SerializedName("paymentMethod")   val paymentMethod:   String  // "COD" | "ONLINE"
)

// ── Response ──────────────────────────────────────────────────

data class OrderItemDto(
    @SerializedName("product")    val product:    OrderProductDto?,
    @SerializedName("sku")        val sku:        String,
    @SerializedName("quantity")   val quantity:   Int,
    @SerializedName("price")      val price:      Double,
    @SerializedName("attributes") val attributes: Map<String, String>?
)

data class OrderProductDto(
    @SerializedName("_id")        val id:         String,
    @SerializedName("name")       val name:       String,
    @SerializedName("coverImage") val coverImage: String
)

data class ShippingAddressDto(
    @SerializedName("fullName")    val fullName:    String,
    @SerializedName("phone")       val phone:       String,
    @SerializedName("addressLine") val addressLine: String,
    @SerializedName("city")        val city:        String,
    @SerializedName("state")       val state:       String,
    @SerializedName("pincode")     val pincode:     String,
    @SerializedName("landmark")    val landmark:    String?
)

data class OrderDto(
    @SerializedName("_id")             val id:              String,
    @SerializedName("items")           val items:           List<OrderItemDto>,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddressDto?,
    @SerializedName("status")          val status:          String,
    @SerializedName("paymentMethod")   val paymentMethod:   String,
    @SerializedName("paymentStatus")   val paymentStatus:   String?,
    @SerializedName("totalAmount")     val totalAmount:     Double,
    @SerializedName("createdAt")       val createdAt:       String
) {
    fun statusLabel(): String = when (status) {
        "PENDING"    -> "Pending"
        "CONFIRMED"  -> "Confirmed"
        "SHIPPED"    -> "Shipped"
        "DELIVERED"  -> "Delivered"
        "CANCELLED"  -> "Cancelled"
        else         -> status
    }
}

data class OrderListResponse(
    @SerializedName("page")   val page:   Int,
    @SerializedName("total")  val total:  Int,
    @SerializedName("orders") val orders: List<OrderDto>
)