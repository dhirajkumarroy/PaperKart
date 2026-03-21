package com.example.paperkart.data.api

import com.example.paperkart.data.dto.order.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {

    // ─── COD Flow ────────────────────────────────────────────────
    // Path: /api/orders/cod/send-otp
    @POST("orders/cod/send-otp")
    suspend fun sendCodOtp(
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("orders/cod/verify-otp")
    suspend fun verifyCodOtp(
        @Body body: Map<String, String>
    ): Response<Unit>

    // ─── Order Actions ───────────────────────────────────────────
    // Path: /api/orders
    @POST("orders")
    suspend fun placeOrder(
        @Header("idempotency-key") idempotencyKey: String,
        @Body request: PlaceOrderRequest
    ): Response<OrderDto>

    // Path: /api/orders/my
    @GET("orders/my")
    suspend fun getMyOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<OrderListResponse>

    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") orderId: String
    ): Response<OrderDto>

    @DELETE("orders/{id}")
    suspend fun cancelOrder(
        @Path("id") orderId: String
    ): Response<Unit>

    // ─── Post-Order ──────────────────────────────────────────────

    // FIX: Your backend uses router.post("/:id/confirm-payment"), NOT PATCH
    @POST("orders/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") orderId: String,
        @Body body: Map<String, String>
    ): Response<OrderDto>

    @GET("orders/{id}/shipment")
    suspend fun getShipmentDetails(
        @Path("id") orderId: String
    ): Response<ShipmentDto>
}