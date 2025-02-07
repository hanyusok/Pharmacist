package com.example.pharmacist.data.mapper

import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderItem
import com.example.pharmacist.domain.model.OrderStatus

fun OrderDto.toOrder(items: List<OrderItemDto>): Order {
    return Order(
        id = id,
        userId = userId,
        status = OrderStatus.valueOf(status),
        items = items.map { it.toOrderItem() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun OrderItemDto.toOrderItem(): OrderItem {
    return OrderItem(
        id = id,
        drugId = drugId,
        quantity = quantity,
        price = price
    )
}

fun Order.toDto(): OrderDto {
    return OrderDto(
        id = id,
        userId = userId,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun OrderItem.toDto(orderId: String): OrderItemDto {
    return OrderItemDto(
        id = id,
        orderId = orderId,
        drugId = drugId,
        quantity = quantity,
        price = price
    )
}