package com.example.pharmacist.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacist.data.repository.DrugRepositoryImpl
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.repository.DrugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrugDetailViewModel @Inject constructor(
    private val repository: DrugRepositoryImpl,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val drugId: String = checkNotNull(savedStateHandle["drugId"])
    
    // Add a refresh trigger in SavedStateHandle
    private val shouldRefresh = savedStateHandle.getStateFlow("shouldRefresh", true)

    private val _drug = MutableStateFlow<Drug?>(null)
    val drug: StateFlow<Drug?> = _drug.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe shouldRefresh changes
            shouldRefresh.collect { shouldRefresh ->
                if (shouldRefresh) {
                    loadDrug()
                    // Reset the refresh trigger
                    savedStateHandle["shouldRefresh"] = false
                }
            }
        }
    }

    fun refresh() {
        savedStateHandle["shouldRefresh"] = true
    }

    private fun loadDrug() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _drug.value = repository.getDrugById(drugId)
                Log.d("DrugDetailViewModel", "Drug reloaded: ${_drug.value}")
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 