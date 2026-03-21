package com.example.paperkart.features.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.dto.product.ProductDto
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repo: ProductRepository
) : ViewModel() {

    val products = MutableLiveData<List<ProductDto>>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun loadProducts() {
        loading.value = true

        viewModelScope.launch {
            try {
                val result = repo.getProducts()

                if (result != null) {
                    products.postValue(result.products)
                } else {
                    error.postValue("Failed to load products")
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            }

            loading.postValue(false)
        }
    }
}