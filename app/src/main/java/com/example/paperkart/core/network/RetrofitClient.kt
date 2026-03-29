package com.example.paperkart.core.network

import android.content.Context
import com.example.paperkart.core.utils.Constants
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var retrofit: Retrofit? = null

    /**
     * Main Retrofit instance used by Repositories.
     * Includes Interceptor (to add tokens) and Authenticator (to refresh tokens).
     */
    fun getInstance(context: Context): Retrofit {
        if (retrofit != null) return retrofit!!

        val sessionManager = SessionManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            // 1. Adds "Authorization: Bearer <token>" to every request
            .addInterceptor(ApiInterceptor(context))

            // 2. Debug logging
            .addInterceptor(logging)

            // 3. The "Stay Logged In" Logic: Triggered on 401 errors
            .authenticator(TokenAuthenticator(context, sessionManager))

            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit!!
    }

    /**
     * Helper instance specifically for the TokenAuthenticator.
     * * IMPORTANT: This must NOT have the Authenticator or ApiInterceptor attached,
     * otherwise a failed refresh call could cause an infinite 401 loop.
     */
    fun getAuthService(context: Context): AuthApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}