package com.example.paperkart.core.utils

object Constants {
    // ✅ Current Working IP of your MacBook
    private const val IP_ADDRESS = "192.168.0.197"

    // ✅ API Base (Used by Retrofit)
    const val BASE_URL = "http://$IP_ADDRESS:3000/api/"

    // ✅ Image Base (Points directly to the uploads route)
    // This allows your Adapter to just append "books/filename.jpg"
    const val IMAGE_BASE_URL = "http://$IP_ADDRESS:3000/uploads/"
}