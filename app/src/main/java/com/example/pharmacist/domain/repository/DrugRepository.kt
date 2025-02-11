package com.example.pharmacist.domain.repository

import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.model.DrugId

interface DrugRepository {
    suspend fun getDrugs(): List<Drug>
    suspend fun searchDrugs(query: String): List<Drug>
    suspend fun getDrugById(id: DrugId): Drug?
    suspend fun updateDrug(drug: Drug): Drug
    suspend fun createDrug(drug: Drug): Drug
    suspend fun deleteDrug(drugId: DrugId): Unit
} 