package com.example.pharmacist.data.repository

import android.util.Log
import com.example.pharmacist.data.dto.OrderDto
import com.example.pharmacist.data.dto.OrderItemDto
import com.example.pharmacist.data.dto.toDto
import com.example.pharmacist.data.mapper.toOrder
import com.example.pharmacist.domain.model.Order
import com.example.pharmacist.domain.model.OrderStatus
import com.example.pharmacist.domain.repository.OrderRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Columns.Companion.raw
import java.lang.System.`in`
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : OrderRepository {

    override suspend fun getOrders(): List<Order> {
        try {
            Log.d("OrderRepositoryImpl", "Starting to fetch orders")
            
            // First, get all orders
            val orders = client.postgrest["orders"]
                .select(columns = Columns.list(
                    "id",
                    "user_id",
                    "status",
                    "created_at",
                    "updated_at",
                    "total_amount",
                    "item_count"
                ))
                .decodeList<OrderDto>()
            
            Log.d("OrderRepositoryImpl", "Fetched ${orders.size} orders")
            
            if (orders.isEmpty()) {
                return emptyList()
            }
            
            // Get all order items with proper filter and include drug details
            val allItems = client.postgrest["order_items"]
                .select(columns = Columns.list(
                    "id",
                    "order_id",
                    "drug_id",
                    "quantity",
                    "price",
                    "drug:drugs(id, drug_name, manufacturer)"
                )) {
                    filter {
                        // Using a simpler approach with a single filter
                        val orderIds = orders.joinToString(",") { "'${it.id}'" }
                        raw("order_id.in.($orderIds)")
                    }
                }
                .decodeList<OrderItemDto>()
            
            Log.d("OrderRepositoryImpl", "Fetched ${allItems.size} order items")
            
            // Group items by order ID for efficient lookup
            val itemsByOrderId = allItems.groupBy { it.orderId }
            
            // Log item distribution for debugging
            itemsByOrderId.forEach { (orderId, items) ->
                Log.d("OrderRepositoryImpl", "Order $orderId has ${items.size} items")
            }
            
            return orders.map { orderDto ->
                orderDto.toOrder(itemsByOrderId[orderDto.id] ?: emptyList()).also {
                    if (itemsByOrderId[orderDto.id].isNullOrEmpty()) {
                        Log.w("OrderRepositoryImpl", "No items found for order ${orderDto.id}")
                    } else {
                        Log.d("OrderRepositoryImpl", 
                            "Mapped order ${orderDto.id} with ${it.items.size} items, " +
                            "total amount: ${it.totalAmount}"
                        )
                    }
                }
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
        try {
            Log.d("OrderRepositoryImpl", 
                "Creating order with ${order.items.size} items, " +
                "total amount: ${order.totalAmount}")
            
            // First create the order with total amount
            val orderDto = OrderDto(
                id = order.id,
                userId = order.userId,
                status = order.status.name,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                totalAmount = order.totalAmount,  // Make sure this is passed
                itemCount = order.itemCount
            )
            
            val createdOrder = client.postgrest["orders"]
                .insert(orderDto)
                .decodeSingle<OrderDto>()
            
            Log.d("OrderRepositoryImpl", 
                "Created order with ID: ${createdOrder.id}, " +
                "total amount: ${createdOrder.totalAmount}")
            
            // Then create all order items
            try {
                order.items.forEach { item ->
                    val orderItemDto = OrderItemDto(
                        id = item.id,
                        orderId = createdOrder.id,
                        drugId = item.drugId.value,
                        quantity = item.quantity,
                        price = item.price
                    )
                    
                    client.postgrest["order_items"]
                        .insert(orderItemDto)
                        .decodeSingle<OrderItemDto>()
                        .also {
                            Log.d("OrderRepositoryImpl", 
                                "Created order item: ${it.id}, " +
                                "price: ${it.price}, quantity: ${it.quantity}")
                        }
                }
            } catch (e: Exception) {
                Log.e("OrderRepositoryImpl", "Error creating order items", e)
                deleteOrder(createdOrder.id)
                throw e
            }
            
            // Fetch the complete order to verify totals
            return getOrderById(createdOrder.id) 
                ?: throw IllegalStateException("Failed to retrieve created order")
        } catch (e: Exception) {
            Log.e("OrderRepositoryImpl", "Error in createOrder", e)
            throw e
        }
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