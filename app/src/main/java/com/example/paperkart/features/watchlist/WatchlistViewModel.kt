package com.example.paperkart.features.watchlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.api.WatchlistApi
import com.example.paperkart.data.api.WatchlistItemDto
import kotlinx.coroutines.launch

class WatchlistViewModel(private val api: WatchlistApi) : ViewModel() {

    val watchlistItems = MutableLiveData<List<WatchlistItemDto>>()
    val isWatched = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()

    fun fetchWatchlist() {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = api.getWatchlist()
                if (response.isSuccessful) {
                    watchlistItems.postValue(response.body() ?: emptyList())
                } else {
                    error.postValue("Failed to load watchlist")
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun checkWatchStatus(productId: String) {
        viewModelScope.launch {
            try {
                val response = api.checkWatchlist(productId)
                if (response.isSuccessful) {
                    isWatched.postValue(response.body()?.watched ?: false)
                }
            } catch (e: Exception) {
                // Silent fail for check
            }
        }
    }

    fun toggleWatchlist(productId: String) {
        viewModelScope.launch {
            try {
                val currentStatus = isWatched.value ?: false
                if (currentStatus) {
                    val response = api.removeFromWatchlist(productId)
                    if (response.isSuccessful) {
                        isWatched.postValue(false)
                    }
                } else {
                    val response = api.addToWatchlist(mapOf("productId" to productId))
                    if (response.isSuccessful) {
                        isWatched.postValue(true)
                    }
                }
            } catch (e: Exception) {
                error.postValue("Action failed: ${e.localizedMessage}")
            }
        }
    }
}
