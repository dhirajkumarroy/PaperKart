package com.example.paperkart.features.cart

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.dto.cart.CartResponse
import kotlinx.coroutines.launch

class CartViewModel(private val repo: CartRepository) : ViewModel() {

    val cart = MutableLiveData<CartResponse?>()
    val totalPrice = MutableLiveData<Double>(0.0)

    // Fixed: Changed 'loading' to 'isLoading' to match Fragment reference
    val isLoading = MutableLiveData<Boolean>(false)

    // Added: Missing error LiveData for the Fragment's Toast
    val error = MutableLiveData<String?>(null)

    fun loadCart() {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repo.getCart()
                if (response.isSuccessful) {
                    val data = response.body()
                    cart.postValue(data)
                    calculateTotal(data)
                } else {
                    error.postValue("Failed to load cart: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue("Connection error: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun removeItem(productId: String, sku: String) {
        isLoading.value = true // Show loading while deleting
        viewModelScope.launch {
            try {
                val response = repo.removeItem(productId, sku)
                if (response.isSuccessful) {
                    loadCart() // Refresh list after removal
                } else {
                    error.postValue("Could not remove item")
                    isLoading.postValue(false)
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
                isLoading.postValue(false)
            }
        }
    }

    private fun calculateTotal(data: CartResponse?) {
        // Ensure priceAtTime exists in your DTO; if not, use 'price'
        val total = data?.items?.sumOf { it.priceAtTime * it.quantity } ?: 0.0
        totalPrice.postValue(total)
    }
}