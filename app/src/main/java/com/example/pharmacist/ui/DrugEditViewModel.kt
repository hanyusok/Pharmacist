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
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.delay

@HiltViewModel
class DrugEditViewModel @Inject constructor(
    private val repository: DrugRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _drug = MutableStateFlow<Drug?>(
        // Initialize with empty Drug for new creation
        Drug(
            id = "",
            mainCode = "",
            drugName = "",
            ingredient = "",
            drugCode = "",
            manufacturer = "",
            isCoveredByInsurance = false
        )
    )
    val drug: StateFlow<Drug?> = _drug.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadDrug(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _drug.value = repository.getDrugById(id) ?: Drug(
                    id = "",
                    mainCode = "",
                    drugName = "",
                    ingredient = "",
                    drugCode = "",
                    manufacturer = "",
                    isCoveredByInsurance = false
                )
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
                
                val drugToSave = _drug.value ?: throw IllegalStateException("No drug to save")
                Log.d("DrugEditViewModel", "Attempting to save drug: $drugToSave")
                
                try {
                    val savedDrug = if (drugToSave.id.isNullOrEmpty()) {
                        // Create new drug
                        repository.createDrug(drugToSave)
                    } else {
                        // Update existing drug
                        repository.updateDrug(drugToSave)
                    }
                    
                    Log.d("DrugEditViewModel", "Drug saved successfully: $savedDrug")
                    _drug.value = savedDrug
                    onUpdateComplete()
                    onSuccess()
                    
                } catch (e: Exception) {
                    Log.e("DrugEditViewModel", "Error saving drug", e)
                    _error.value = "Failed to save drug: ${e.message}"
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

    fun createNewDrug(drug: Drug, onSuccess: () -> Unit, onUpdateComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("DrugEditViewModel", "Creating new drug: $drug")
                
                try {
                    val createdDrug = repository.createDrug(drug)
                    Log.d("DrugEditViewModel", "Drug created successfully: $createdDrug")
                    
                    _drug.value = createdDrug
                    onUpdateComplete()
                    onSuccess()
                    
                } catch (e: Exception) {
                    Log.e("DrugEditViewModel", "Error creating drug", e)
                    _error.value = "Failed to create drug: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDrugFields(
        mainCode: String = _drug.value?.mainCode ?: "",
        drugName: String = _drug.value?.drugName ?: "",
        ingredient: String = _drug.value?.ingredient ?: "",
        drugCode: String = _drug.value?.drugCode ?: "",
        manufacturer: String = _drug.value?.manufacturer ?: "",
        isCoveredByInsurance: Boolean = _drug.value?.isCoveredByInsurance ?: false
    ) {
        _drug.value = Drug(
            id = _drug.value?.id ?: "",
            mainCode = mainCode,
            drugName = drugName,
            ingredient = ingredient,
            drugCode = drugCode,
            manufacturer = manufacturer,
            isCoveredByInsurance = isCoveredByInsurance
        )
    }

    fun setError(message: String) {
        _error.value = message
    }
} 