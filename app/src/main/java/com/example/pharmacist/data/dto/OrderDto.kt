package com.example.pharmacist.data.dto

import com.example.pharmacist.data.mapper.toDrug
import com.example.pharmacist.data.mapper.toDto
import com.example.pharmacist.domain.model.DrugId
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderItem
import com.example.pharmacist.domain.model.OrderStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
//import java.math.BigDecimal

@Serializable
data class OrderDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("status")
    val status: String = OrderStatus.PENDING.name,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("item_count")
    val itemCount: Int = 0
) {
    init {
        require(status in OrderStatus.values().map { it.name }) { 
            "Invalid order status: $status" 
        }
    }
}

@Serializable
data class OrderItemDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("order_id")
    val orderId: String = "",
    @SerialName("drug_id")
    val drugId: String = "",
    @SerialName("quantity")
    val quantity: Int = 1,
    @SerialName("price")
    val price: Double = 0.0,
    @SerialName("drug")
    val drug: DrugDto? = null
) {
    init {
        require(quantity > 0) { "Quantity must be greater than 0" }
        require(price >= 0.0) { "Price must be greater than or equal to 0" }
    }
}

//@Serializer(forClass = BigDecimal::class)
//object BigDecimalSerializer : KSerializer<BigDecimal> {
//    override fun serialize(encoder: Encoder, value: BigDecimal) {
//        encoder.encodeString(value.toPlainString())
//    }
//
//    override fun deserialize(decoder: Decoder): BigDecimal {
//        return BigDecimal(decoder.decodeString())
//    }
//}

fun Order.toDto(): OrderDto {
    return OrderDto(
        id = id,
        userId = userId,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        totalAmount = totalAmount,
        itemCount = itemCount
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