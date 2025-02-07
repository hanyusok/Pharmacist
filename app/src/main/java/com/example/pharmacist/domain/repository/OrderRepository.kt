package com.example.pharmacist.domain.repository

import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderStatus

interface OrderRepository {
    suspend fun getOrders(): List<Order>
    suspend fun getOrderById(orderId: String): Order?
    suspend fun createOrder(order: Order): Order
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
    suspend fun deleteOrder(orderId: String)
}