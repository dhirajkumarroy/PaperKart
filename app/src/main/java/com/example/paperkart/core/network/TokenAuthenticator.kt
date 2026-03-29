package com.example.paperkart.core.network

import android.content.Context
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.dto.auth.RefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking

class TokenAuthenticator(
    private val context: Context,
    private val session: SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. Get the stored refresh token. If null, we can't refresh.
        val refreshToken = session.getRefreshToken() ?: return null

        // 2. Use the "Clean" Retrofit instance to avoid infinite 401 loops
        val authService = RetrofitClient.getAuthService(context)

        // 3. Perform the refresh call synchronously
        return runBlocking {
            try {
                val res = authService.refreshAccessToken(RefreshRequest(refreshToken))

                if (res.isSuccessful && res.body()?.data != null) {
                    val newData = res.body()!!.data!!

                    // 4. Save the brand new tokens
                    session.saveToken(newData.accessToken)
                    session.saveRefreshToken(newData.refreshToken)

                    // 5. Retry the original failed request with the NEW Access Token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newData.accessToken}")
                        .build()
                } else {
                    // Refresh token is also expired or invalid -> Force Logout
                    handleLogout()
                    null
                }
            } catch (e: Exception) {
                handleLogout()
                null
            }
        }
    }

    private fun handleLogout() {
        session.clearSession()
        // Optional: Send a broadcast or start AuthActivity to notify the user
    }
}