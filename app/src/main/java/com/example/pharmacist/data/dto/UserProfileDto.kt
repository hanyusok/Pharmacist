package com.example.pharmacist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
) 