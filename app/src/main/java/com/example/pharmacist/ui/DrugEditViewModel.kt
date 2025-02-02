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

    fun saveDrug(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _drug.value?.let { drug ->
                    repository.updateDrug(drug)
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 