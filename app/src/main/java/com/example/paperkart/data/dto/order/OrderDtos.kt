package com.example.paperkart.data.dto.order

import com.google.gson.annotations.SerializedName

// ── REQUEST DTOS (Sent to Server) ──────────────────────────────

data class ShippingAddressRequest(
    @SerializedName("name")     val name:     String,
    @SerializedName("phone")    val phone:    String,
    @SerializedName("address")  val address:  String,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("city")     val city:     String,
    @SerializedName("state")    val state:    String,
    @SerializedName("pincode")  val pincode:  String
)

data class PlaceOrderRequest(
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddressRequest,
    @SerializedName("paymentMethod")   val paymentMethod:   String, // "COD" or "ONLINE"
    @SerializedName("couponCode")      val couponCode:      String? = null
)

// ── RESPONSE DTOS (Received from Server) ───────────────────────

data class ShippingAddressDto(
    @SerializedName("name")     val name:     String,
    @SerializedName("phone")    val phone:    String,
    @SerializedName("address")  val address:  String,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("city")     val city:     String,
    @SerializedName("state")    val state:    String,
    @SerializedName("pincode")  val pincode:  String
)

data class PaymentDto(
    @SerializedName("method")            val method: String,
    @SerializedName("status")            val status: String,
    @SerializedName("razorpayOrderId")   val razorpayOrderId: String?,
    @SerializedName("razorpayPaymentId") val razorpayPaymentId: String?,
    @SerializedName("razorpaySignature") val razorpaySignature: String?, // Added for online verification
    @SerializedName("paidAt")            val paidAt: String?
)

data class ShipmentDto(
    @SerializedName("courier")     val courier: String?,
    @SerializedName("awb")         val awb: String?,
    @SerializedName("trackingUrl") val trackingUrl: String?,
    @SerializedName("labelUrl")    val labelUrl: String?
)

data class OrderItemDto(
    @SerializedName("title")    val title: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price")    val price: Double,
    @SerializedName("image")    val image: String? // Added: Useful for showing product icons in Order List
)

data class OrderDto(
    @SerializedName("_id")             val id: String,
    @SerializedName("items")           val items: List<OrderItemDto>,
    @SerializedName("totalAmount")     val totalAmount: Double,
    @SerializedName("status")          val status: String, // e.g., "PLACED", "SHIPPED"
    @SerializedName("payment")         val payment: PaymentDto,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddressDto,
    @SerializedName("shipment")        val shipment: ShipmentDto?,
    @SerializedName("createdAt")       val createdAt: String
)

data class OrderListResponse(
    @SerializedName("totalOrders") val totalOrders: Int,
    @SerializedName("data")        val orders: List<OrderDto>,
    @SerializedName("totalPages")  val totalPages: Int,
    @SerializedName("currentPage") val currentPage: Int // Added: Crucial for pagination logic
)