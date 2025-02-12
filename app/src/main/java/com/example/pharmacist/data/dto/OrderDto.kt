package com.example.pharmacist.data.dto

import com.example.pharmacist.domain.model.DrugId
import com.example.pharmacist.domain.model.OrderItem
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
    val updatedAt: String = "",
    @SerialName("items")
    val items: List<OrderItemDto> = emptyList()
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

fun OrderItemDto.toOrderItem(): OrderItem {
    return OrderItem(
        id = id,
        drugId = DrugId(drugId),  // Convert String to DrugId
        quantity = quantity,
        price = price
    )
}

fun OrderItem.toDto(): OrderItemDto {
    return OrderItemDto(
        id = id,
        drugId = drugId.value,  // Convert DrugId to String
        quantity = quantity,
        price = price
    )
} 