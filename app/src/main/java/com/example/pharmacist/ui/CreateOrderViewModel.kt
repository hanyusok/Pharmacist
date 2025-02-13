package com.example.pharmacist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacist.domain.model.*
import com.example.pharmacist.domain.repository.DrugRepository
import com.example.pharmacist.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject
import android.util.Log
import com.example.pharmacist.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import java.time.ZoneOffset

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val drugRepository: DrugRepository,
    private val client: SupabaseClient
) : ViewModel() {
    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: StateFlow<List<Drug>> = _drugs.asStateFlow()

    private val _selectedDrugs = MutableStateFlow<Map<DrugId, Int>>(emptyMap())
    val selectedDrugs: StateFlow<Map<DrugId, Int>> = _selectedDrugs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDrugs()
    }

    private fun loadDrugs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _drugs.value = drugRepository.getDrugs()
            } catch (e: Exception) {
                _error.value = "Failed to load drugs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDrugQuantity(drugId: DrugId, quantity: Int) {
        val currentMap = _selectedDrugs.value.toMutableMap()
        if (quantity <= 0) {
            currentMap.remove(drugId)
        } else {
            currentMap[drugId] = quantity
        }
        _selectedDrugs.value = currentMap
    }

    fun createOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // First check if user is authenticated
                val userId = try {
                    getUserId().toString()
                } catch (e: IllegalStateException) {
                    _error.value = e.message
                    return@launch
                }

                Log.d("CreateOrderViewModel", "Creating order for user: $userId")

                // Rest of the order creation logic
                if (!validateDrugs(_selectedDrugs.value)) {
                    _error.value = "One or more selected drugs no longer exist"
                    return@launch
                }

                val orderItems = _selectedDrugs.value.map { (drugId, quantity) ->
                    val drug = _drugs.value.find { it.id == drugId }
                        ?: throw IllegalStateException("Drug not found")
                    
                    val itemPrice = calculateDrugPrice(drug)
                    
                    OrderItem(
                        id = "",
                        orderId = "",
                        drugId = drugId,
                        quantity = quantity,
                        price = itemPrice,
                        drug = drug
                    )
                }

                val totalAmount = orderItems.sumOf { it.price * it.quantity }
                
                val order = Order(
                    id = "",
                    userId = userId,  // Using the authenticated user's ID
                    status = OrderStatus.PENDING,
                    items = orderItems,
                    createdAt = OffsetDateTime.now(ZoneOffset.UTC).toString(),
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC).toString(),
                    totalAmount = totalAmount,
                    itemCount = orderItems.size
                )

                Log.d("CreateOrderViewModel", 
                    "Creating order with ${order.items.size} items, " +
                    "total: $totalAmount for user: $userId")

                orderRepository.createOrder(order)
                onSuccess()
            } catch (e: Exception) {
                Log.e("CreateOrderViewModel", "Error creating order", e)
                _error.value = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Failed to create order: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateDrugPrice(drug: Drug): Double {
        // TODO: Implement proper price calculation based on your business logic
        return when {
            drug.isCoveredByInsurance -> 5.0  // Example: lower price for insured drugs
            else -> 10.0  // Default price
        }.also {
            Log.d("CreateOrderViewModel", 
                "Calculated price for ${drug.drugName}: $it " +
                "(covered by insurance: ${drug.isCoveredByInsurance})")
        }
    }

    private fun getUserId(): String {
        return client.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated user found. Please log in.")
    }

    private suspend fun validateDrugs(selectedDrugs: Map<DrugId, Int>): Boolean {
        return selectedDrugs.all { (drugId, _) ->
            _drugs.value.any { it.id == drugId }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 