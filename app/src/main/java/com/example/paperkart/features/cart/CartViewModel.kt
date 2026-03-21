package com.example.paperkart.features.cart

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.dto.cart.CartResponse
import kotlinx.coroutines.launch

class CartViewModel(private val repo: CartRepository) : ViewModel() {

    val cart = MutableLiveData<CartResponse?>()
    val loading = MutableLiveData<Boolean>()
    val totalPrice = MutableLiveData<Double>()

    fun loadCart() {
        loading.value = true
        viewModelScope.launch {
            try {
                val response = repo.getCart()
                if (response.isSuccessful) {
                    val data = response.body()
                    cart.postValue(data)
                    calculateTotal(data)
                }
            } catch (e: Exception) { /* Handle error */ }
            loading.postValue(false)
        }
    }

    fun removeItem(productId: String, sku: String) {
        viewModelScope.launch {
            val response = repo.removeItem(productId, sku)
            if (response.isSuccessful) {
                loadCart() // Refresh list after removal
            }
        }
    }

    private fun calculateTotal(data: CartResponse?) {
        val total = data?.items?.sumOf { it.priceAtTime * it.quantity } ?: 0.0
        totalPrice.postValue(total)
    }
}