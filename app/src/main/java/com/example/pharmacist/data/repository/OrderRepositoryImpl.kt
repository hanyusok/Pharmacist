package com.example.pharmacist.data.repository

import android.util.Log
import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
import com.example.pharmacist.data.mapper.toOrder
import com.example.pharmacist.data.mapper.toDto
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderStatus
import com.example.pharmacist.domain.repository.OrderRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : OrderRepository {

    override suspend fun getOrders(): List<Order> {
        val orders = client.postgrest["orders"]
            .select()
            .decodeList<OrderDto>()
            
        return orders.map { orderDto ->
            val items = client.postgrest["order_items"]
                .select {
                    filter {
                        eq("order_id", orderDto.id) }
                    }
                .decodeList<OrderItemDto>()
            orderDto.toOrder(items)
        }
    }

    override suspend fun getOrderById(orderId: String): Order? {
        val order = client.postgrest["orders"]
            .select {
                filter {
                    eq("id", orderId)
                }
            }
            .decodeSingleOrNull<OrderDto>() ?: return null
            
        val items = client.postgrest["order_items"]
            .select {
                filter {
                    eq("order_id", orderId)
                }
            }
            .decodeList<OrderItemDto>()
            
        return order.toOrder(items)
    }

    override suspend fun createOrder(order: Order): Order {
        val orderDto = order.toDto()
        
        val createdOrder = client.postgrest["orders"]
            .insert(orderDto)
            .decodeSingle<OrderDto>()
            
        // Insert order items
        order.items.forEach { item ->
            client.postgrest["order_items"]
                .insert(item.toDto(createdOrder.id))
        }
        
        return getOrderById(createdOrder.id) 
            ?: throw IllegalStateException("Failed to retrieve created order")
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        client.postgrest["orders"]
            .update({
                set("status", status.name)
                set("updated_at", java.time.OffsetDateTime.now().toString())
            }) {
                filter {
                    eq("id", orderId)
                }
            }
    }

    override suspend fun deleteOrder(orderId: String) {
        // Delete order items first due to foreign key constraints
        client.postgrest["order_items"]
            .delete {
                filter {
                    eq("order_id", orderId)
                }
            }
            
        // Then delete the order
        client.postgrest["orders"]
            .delete {
                filter {
                    eq("id", orderId)
                }
            }
    }
} 