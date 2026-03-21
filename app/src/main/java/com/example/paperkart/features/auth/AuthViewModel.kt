package com.example.paperkart.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperkart.data.dto.user.UserDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    // ✅ loginState now correctly holds the UserDto returned from the repo
    private val _loginState = MutableLiveData<UserDto>()
    val loginState: LiveData<UserDto> = _loginState

    private val _registerSuccess = MutableLiveData<String>()
    val registerSuccess: LiveData<String> = _registerSuccess

    private val _forgotSuccess = MutableLiveData<String>()
    val forgotSuccess: LiveData<String> = _forgotSuccess

    private val _resetSuccess = MutableLiveData<String>()
    val resetSuccess: LiveData<String> = _resetSuccess

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // OTP specific
    private val _otpSent = MutableLiveData(false)
    val otpSent: LiveData<Boolean> = _otpSent

    private val _otpTimer = MutableLiveData(0)
    val otpTimer: LiveData<Int> = _otpTimer

    private var timerJob: Job? = null

    // ── Login ─────────────────────────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            // The Repository now handles the "USER" role restriction internally
            repo.login(email, password)
                .onSuccess { userDto ->
                    _loginState.postValue(userDto)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "Login failed")
                }
            _isLoading.postValue(false)
        }
    }

    // ── Register ──────────────────────────────────────────────
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            repo.register(name, email, password)
                .onSuccess { msg ->
                    _registerSuccess.postValue(msg)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "Registration failed")
                }
            _isLoading.postValue(false)
        }
    }

    // ── OTP Logic ─────────────────────────────────────────────
    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            repo.sendOtp(phone)
                .onSuccess {
                    _otpSent.postValue(true)
                    startOtpCountdown(60)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "Failed to send OTP")
                }
            _isLoading.postValue(false)
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            repo.verifyOtp(phone, otp)
                .onSuccess { userDto ->
                    _loginState.postValue(userDto)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "OTP verification failed")
                }
            _isLoading.postValue(false)
        }
    }

    private fun startOtpCountdown(seconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _otpTimer.postValue(remaining)
                if (remaining > 0) delay(1000L)
            }
        }
    }

    // ── Password Recovery ─────────────────────────────────────
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            repo.forgotPassword(email)
                .onSuccess { msg ->
                    _forgotSuccess.postValue(msg)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "Failed to send reset email")
                }
            _isLoading.postValue(false)
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            repo.resetPassword(token, newPassword)
                .onSuccess { msg ->
                    _resetSuccess.postValue(msg)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "Password reset failed")
                }
            _isLoading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}