package com.example.paperkart.data.dto.product

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val productType: String,
    val coverImage: String?,
    val variants: List<VariantDto>?,
    val ratings: RatingsDto?
) {
    // Helper to show the starting price in lists
    val minPrice: Double
        get() = variants?.minOfOrNull { it.price } ?: 0.0
}

data class VariantDto(
    val sku: String,   // ✅ Added: This was missing and causing the error
    val price: Double,
    val stock: Int,
    val reservedStock: Int? = 0
)

data class RatingsDto(
    val average: Double,
    val count: Int
)