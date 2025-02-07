package com.example.pharmacist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("status")
    val status: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class OrderItemDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("order_id")
    val orderId: String = "",
    @SerialName("drug_id")
    val drugId: String = "",
    @SerialName("quantity")
    val quantity: Int = 0,
    @SerialName("price")
    val price: Double = 0.0
) 