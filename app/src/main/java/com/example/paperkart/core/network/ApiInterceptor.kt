package com.example.paperkart.core.network

import android.content.Context
import com.example.paperkart.core.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor(context: Context) : Interceptor {

    private val session = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder = request.newBuilder()

        session.getToken()?.let {
            builder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(builder.build())
    }
}