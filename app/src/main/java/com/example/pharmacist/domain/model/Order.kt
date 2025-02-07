package com.example.pharmacist.domain.model

data class Order(
    val id: String,
    val userId: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val createdAt: String,
    val updatedAt: String
)

data class OrderItem(
    val id: String,
    val drugId: String,
    val quantity: Int,
    val price: Double
) 