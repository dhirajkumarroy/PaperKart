package com.example.paperkart.data.dto.product

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val productType: String,

    // 🔥 FIXED: Changed from String to ImageDto object
    val coverImage: ImageDto?,

    // 🔥 ADDED: This matches the JSON field directly
    val priceRange: PriceRangeDto?,

    val variants: List<VariantDto>?,
    val ratings: RatingsDto?,
    val status: String?
) {
    // Helper to get price from the pre-computed server range
    val minPrice: Double
        get() = priceRange?.min ?: variants?.minOfOrNull { it.price } ?: 0.0
}

data class ImageDto(
    val url: String?,
    val public_id: String?
)

data class PriceRangeDto(
    val min: Double,
    val max: Double
)

data class VariantDto(
    val sku: String,
    val price: Double,
    val stock: Int,
    val reservedStock: Int? = 0
)

data class RatingsDto(
    val average: Double,
    val count: Int
)