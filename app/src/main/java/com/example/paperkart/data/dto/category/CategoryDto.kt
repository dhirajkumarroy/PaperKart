package com.example.paperkart.data.dto.category

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val productCount: Int,
    val subCategories: List<CategoryDto> = emptyList()
)