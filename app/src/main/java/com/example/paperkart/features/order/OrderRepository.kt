package com.example.paperkart.features.order

import com.example.paperkart.data.api.OrderApi
import com.example.paperkart.data.dto.order.PlaceOrderRequest
import com.example.paperkart.data.dto.order.ShippingAddressRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(private val api: OrderApi) {

    // Fix for: Argument type mismatch & missing 'request' parameter
    suspend fun placeOrder(idempotencyKey: String, address: ShippingAddressRequest, method: String) =
        withContext(Dispatchers.IO) {
            val request = PlaceOrderRequest(address, method)
            // Ensure key is 1st, request is 2nd to match OrderApi interface
            api.placeOrder(idempotencyKey, request)
        }

    // Fix for: No value passed for parameter 'body'
    suspend fun sendCodOtp(phone: String) = withContext(Dispatchers.IO) {
        // You MUST pass the phone map because the interface now expects it
        val body = mapOf("phone" to phone)
        api.sendCodOtp(body)
    }

    suspend fun verifyCodOtp(phone: String, otp: String) = withContext(Dispatchers.IO) {
        val body = mapOf("phone" to phone, "otp" to otp)
        api.verifyCodOtp(body)
    }

    suspend fun getMyOrders(page: Int, limit: Int) = withContext(Dispatchers.IO) {
        api.getMyOrders(page, limit)
    }
}