package com.example.paperkart.data.dto.order

import com.example.paperkart.data.dto.product.ImageDto
import com.google.gson.annotations.SerializedName

// ── REQUEST DTOS (Sent to Server) ──────────────────────────────

data class ShippingAddressRequest(
    @SerializedName("name")     val name:     String,
    @SerializedName("phone")    val phone:    String,
    @SerializedName("address")  val address:  String,
    @SerializedName("landmark") val landmark: String? = null,
    @SerializedName("city")     val city:     String = "Varanasi",
    @SerializedName("state")    val state:    String = "UP",
    @SerializedName("pincode")  val pincode:  String = "221005"
)

data class PlaceOrderRequest(
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddressRequest,
    @SerializedName("paymentMethod")   val paymentMethod:   String, // "COD" or "ONLINE"
    @SerializedName("couponCode")      val couponCode:      String? = null
)

// ── RESPONSE DTOS (Received from Server) ───────────────────────

data class ShippingAddressDto(
    val name: String,
    val phone: String,
    val address: String,
    val landmark: String?,
    val city: String,
    val state: String,
    val pincode: String
)

data class PaymentDto(
    val method: String,
    val status: String,
    val razorpayOrderId: String?,
    val razorpayPaymentId: String?,
    val razorpaySignature: String?,
    val paidAt: String?
)

data class ShipmentDto(
    val courier: String?,
    val awb: String?,
    val trackingUrl: String?,
    val labelUrl: String?,
    val status: String?,
    val pickupScheduled: Boolean? = false,
    val pickupScheduledAt: String? = null
)

data class OrderItemDto(
    val product: OrderProductSummaryDto?,
    val quantity: Int,
    val price: Double,
    val sku: String?
)

data class OrderProductSummaryDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val images: List<ImageDto>? // Backend uses .populate("items.product", "name images slug")
)

data class OrderDto(
    @SerializedName("_id") val id: String,
    val user: Any?, // Can be ID or User object depending on population
    val items: List<OrderItemDto>,
    val totalAmount: Double,
    val status: String,
    val payment: PaymentDto,
    val shippingAddress: ShippingAddressDto,
    val shipment: ShipmentDto?,
    val history: List<OrderHistoryDto>?,
    val createdAt: String
)

data class OrderHistoryDto(
    val status: String,
    val message: String,
    val timestamp: String
)

data class OrderListResponse(
    val page: Int,
    val totalPages: Int,
    val totalOrders: Int,
    val data: List<OrderDto>
)
