package com.example.pharmacist.data.mapper

import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderItem
import com.example.pharmacist.domain.model.OrderStatus
import com.example.pharmacist.domain.model.DrugId

fun OrderDto.toOrder(): Order {
    return Order(
        id = id,
        userId = userId,
        status = OrderStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        items = items.map { it.toOrderItem() }
    )
}

fun OrderItemDto.toOrderItem(): OrderItem {
    return OrderItem(
        id = id,
        drugId = DrugId(drugId),
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
        updatedAt = updatedAt,
        items = items.map { it.toDto(id) }
    )
}

fun OrderItem.toDto(orderId: String): OrderItemDto {
    return OrderItemDto(
        id = id,
        orderId = orderId,
        drugId = drugId.value,
        quantity = quantity,
        price = price
    )
}