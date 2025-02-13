package com.example.pharmacist.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    @SerialName("PENDING")
    PENDING,
    @SerialName("PROCESSING")
    PROCESSING,
    @SerialName("COMPLETED")
    COMPLETED,
    @SerialName("CANCELLED")
    CANCELLED
} 