package com.example.pharmacist.domain.model

data class Order(
    val id: String,
    val userId: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val createdAt: String,
    val updatedAt: String,
    val totalAmount: Double = items.sumOf { it.price * it.quantity },
    val itemCount: Int = items.size
)

data class OrderItem(
    val id: String,
    val orderId: String,
    val drugId: DrugId,
    val quantity: Int,
    val price: Double,
    val drug: Drug? = null
) 