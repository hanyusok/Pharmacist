package com.example.pharmacist.data.mapper

import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderItem
import com.example.pharmacist.domain.model.OrderStatus
import com.example.pharmacist.domain.model.DrugId
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun OrderDto.toOrder(items: List<OrderItemDto> = emptyList()): Order {
    return Order(
        id = id,
        userId = userId,
        status = OrderStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        items = items.map { it.toOrderItem() },
        totalAmount = totalAmount ?: items.sumOf { it.price * it.quantity },
        itemCount = itemCount ?: items.size
    )
}

fun Order.toDto(): OrderDto {
    val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
    return OrderDto(
        id = id,
        userId = userId,
        status = status.name,
        createdAt = createdAt.ifEmpty { now },
        updatedAt = updatedAt.ifEmpty { now },
        totalAmount = totalAmount,
        itemCount = itemCount
    )
}

fun OrderItemDto.toOrderItem(): OrderItem {
    return OrderItem(
        id = id,
        orderId = orderId,
        drugId = DrugId(drugId),
        quantity = quantity,
        price = price,
        drug = drug?.toDrug()
    )
}

fun OrderItem.toDto(): OrderItemDto {
    return OrderItemDto(
        id = id,
        orderId = orderId,
        drugId = drugId.value,
        quantity = quantity,
        price = price,
        drug = drug?.toDto()
    )
}