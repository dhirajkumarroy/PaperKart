package com.example.paperkart.features.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.api.NotificationApi
import com.example.paperkart.data.api.NotificationDto
import kotlinx.coroutines.launch

class NotificationViewModel(private val api: NotificationApi) : ViewModel() {

    val notifications = MutableLiveData<List<NotificationDto>>()
    val unreadCount = MutableLiveData<Int>(0)
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun fetchNotifications(page: Int = 1) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = api.getMyNotifications(page)
                if (response.isSuccessful) {
                    notifications.postValue(response.body()?.data ?: emptyList())
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            try {
                val response = api.getUnreadCount()
                if (response.isSuccessful) {
                    unreadCount.postValue(response.body()?.unread ?: 0)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                val response = api.markAsRead(id)
                if (response.isSuccessful) {
                    fetchUnreadCount() // Refresh badge
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                val response = api.markAllAsRead()
                if (response.isSuccessful) {
                    fetchUnreadCount()
                    fetchNotifications()
                }
            } catch (e: Exception) {
                error.postValue("Failed to mark all as read")
            }
        }
    }
}
