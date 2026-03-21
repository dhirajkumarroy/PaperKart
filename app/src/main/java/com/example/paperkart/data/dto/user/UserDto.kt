package com.example.paperkart.data.dto.user

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: UserDataContainer?
)

data class UserDataContainer(
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("_id")           val id: String?,
    @SerializedName("name")          val name: String?,
    @SerializedName("email")         val email: String?,
    @SerializedName("phone")         val phone: String?,

    // ✅ FIX 1: Provide a default "USER" string to avoid null crashes in Auth check
    @SerializedName("role")          val role: String? = "USER",

    @SerializedName("status")        val status: String?,
    @SerializedName("providers")     val providers: List<String>?,
    @SerializedName("emailVerified") val emailVerified: Boolean?,
    @SerializedName("createdAt")     val createdAt: String?,

    // ✅ FIX 2: Added Profile Image URL (Essential for your Profile Screen)
    @SerializedName("profileImage")  val profileImage: String?,

    @SerializedName("savedAddress")  val savedAddress: AddressDto?,
    @SerializedName("profileCompleted") val profileCompleted: ProfileCompletedDto?
)

data class AddressDto(
    @SerializedName("fullName")    val fullName: String?,
    @SerializedName("phone")       val phone: String?,
    @SerializedName("addressLine") val addressLine: String?,
    @SerializedName("city")        val city: String?,
    @SerializedName("state")       val state: String?,
    @SerializedName("pincode")     val pincode: String?,
    @SerializedName("landmark")    val landmark: String?
)

data class ProfileCompletedDto(
    @SerializedName("phone") val phone: Boolean = false,
    @SerializedName("email") val email: Boolean = false
)