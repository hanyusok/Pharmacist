package com.example.pharmacist.data.repository

import android.util.Log
import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
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
        try {
            // First, get all orders
            val orders = client.postgrest["orders"]
                .select()
                .decodeList<OrderDto>()
            
            if (orders.isEmpty()) {
                return emptyList()
            }
            
            // Get all order items with proper filter syntax
            val allItems = client.postgrest["order_items"]
                .select {
                    filter {
                        // Using PostgreSQL's IN operator via Supabase's filter DSL
                        `in`("order_id", orders.map { it.id }.toTypedArray())
                    }
                }
                .decodeList<OrderItemDto>()
            
            // Group items by order ID
            val itemsByOrderId = allItems.groupBy { it.orderId }
            
            return orders.map { orderDto ->
                Order(
                    id = orderDto.id,
                    userId = orderDto.userId,
                    status = OrderStatus.valueOf(orderDto.status),
                    createdAt = orderDto.createdAt,
                    updatedAt = orderDto.updatedAt,
                    items = itemsByOrderId[orderDto.id]?.map { it.toOrderItem() } ?: emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("OrderRepositoryImpl", "Error fetching orders", e)
            throw e
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