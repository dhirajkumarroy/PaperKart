package com.example.paperkart.features.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.api.OrderApi
import com.example.paperkart.data.dto.order.OrderDto
import com.example.paperkart.data.dto.order.PlaceOrderRequest
import com.example.paperkart.data.dto.order.ShippingAddressRequest
import kotlinx.coroutines.launch

class OrderViewModel(private val api: OrderApi) : ViewModel() {

    val orderResult = MutableLiveData<OrderDto?>()
    val userOrders = MutableLiveData<List<OrderDto>>()
    val error = MutableLiveData<String?>()
    val otpSent = MutableLiveData<Boolean>(false)
    val isLoading = MutableLiveData<Boolean>(false)

    /**
     * Reusable private function to handle the actual order placement call.
     * This ensures the 'orderResult' LiveData is updated in one central place.
     */
    private suspend fun executePlaceOrder(address: ShippingAddressRequest, method: String, key: String) {
        try {
            val response = api.placeOrder(key, PlaceOrderRequest(address, method))
            if (response.isSuccessful && response.body() != null) {
                // This is the trigger for Activity navigation
                orderResult.postValue(response.body())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Order Placement Failed"
                error.postValue(errorBody)
            }
        } catch (e: Exception) {
            error.postValue("Placement Error: ${e.localizedMessage}")
        }
    }

    fun placeOrder(address: ShippingAddressRequest, method: String, idempotencyKey: String) {
        viewModelScope.launch {
            isLoading.value = true
            executePlaceOrder(address, method, idempotencyKey)
            isLoading.value = false
        }
    }

    fun sendCodOtp(phone: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = api.sendCodOtp(mapOf("phone" to phone))
                if (response.isSuccessful) {
                    otpSent.postValue(true)
                } else {
                    error.postValue("Could not send OTP. Check your phone number.")
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun verifyAndPlaceOrder(address: ShippingAddressRequest, otp: String, idempotencyKey: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val verifyRes = api.verifyCodOtp(mapOf("phone" to address.phone, "otp" to otp))
                if (verifyRes.isSuccessful) {
                    // Call the helper directly inside the same coroutine
                    executePlaceOrder(address, "COD", idempotencyKey)
                } else {
                    error.postValue("Invalid OTP code. Please try again.")
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun confirmOnlinePayment(orderId: String, paymentId: String, signature: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Ensure these keys match your Node.js confirmPaymentSchema
                val body = mapOf(
                    "paymentId" to paymentId,
                    "razorpaySignature" to signature,
                    "gatewayOrderId" to "N/A" // Add if your backend requires it
                )
                val response = api.confirmPayment(orderId, body)
                if (response.isSuccessful) {
                    orderResult.postValue(response.body())
                } else {
                    error.postValue("Payment verification failed on server")
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchMyOrders(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = api.getMyOrders(page, limit)
                if (response.isSuccessful) {
                    userOrders.postValue(response.body()?.orders ?: emptyList())
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.value = false
            }
        }
    }
}