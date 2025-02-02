package com.example.pharmacist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacist.data.repository.DrugRepositoryImpl
import com.example.pharmacist.domain.model.Drug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.delay

@HiltViewModel
class DrugEditViewModel @Inject constructor(
    private val repository: DrugRepositoryImpl
) : ViewModel() {

    private val _drug = MutableStateFlow<Drug?>(null)
    val drug: StateFlow<Drug?> = _drug.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadDrug(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _drug.value = repository.getDrugById(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDrug(drug: Drug) {
        _drug.value = drug
    }

    fun saveDrug(onSuccess: () -> Unit, onUpdateComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val drugToUpdate = _drug.value ?: throw IllegalStateException("No drug to update")
                Log.d("DrugEditViewModel", "Attempting to update drug: $drugToUpdate")
                
                try {
                    val updatedDrug = repository.updateDrug(drugToUpdate)
                    Log.d("DrugEditViewModel", "Drug updated successfully: $updatedDrug")
                    
                    // Update local state first
                    _drug.value = updatedDrug
                    
                    // Ensure the update is complete before proceeding
                    delay(100) // Small delay to ensure state propagation
                    
                    // Trigger list refresh first
                    onUpdateComplete()
                    
                    // Wait for refresh to complete
                    delay(100) // Small delay to ensure refresh
                    
                    // Then navigate back
                    onSuccess()
                    
                } catch (e: IllegalStateException) {
                    Log.e("DrugEditViewModel", "Database update failed", e)
                    _error.value = "Failed to update drug: ${e.message}"
                } catch (e: Exception) {
                    Log.e("DrugEditViewModel", "Unexpected error during update", e)
                    _error.value = "An unexpected error occurred: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Add a function to verify current state
    fun verifyDrugState() {
        Log.d("DrugEditViewModel", "Current drug state: ${_drug.value}")
    }
} 