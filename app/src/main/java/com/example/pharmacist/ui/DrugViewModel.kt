package com.example.pharmacist.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.repository.DrugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrugViewModel @Inject constructor(
    private val repository: DrugRepository
) : ViewModel() {
    private val _drugs = MutableStateFlow<List<Drug>>(emptyList())
    val drugs: StateFlow<List<Drug>> = _drugs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("DrugViewModel", "ViewModel initialized")
        loadDrugs()
    }

    fun loadDrugs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _drugs.value = repository.getDrugs()
                Log.d("DrugViewModel", "Loaded ${_drugs.value.size} drugs")
            } catch (e: Exception) {
                Log.e("DrugViewModel", "Error loading drugs", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchDrugs(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _drugs.value = repository.searchDrugs(query)
                Log.d("DrugViewModel", "Search completed with ${_drugs.value.size} results")
            } catch (e: Exception) {
                Log.e("DrugViewModel", "Error searching drugs", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}