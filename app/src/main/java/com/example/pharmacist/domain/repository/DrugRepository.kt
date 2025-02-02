package com.example.pharmacist.domain.repository

import com.example.pharmacist.domain.model.Drug

interface DrugRepository {
    suspend fun getDrugs(): List<Drug>
    suspend fun searchDrugs(query: String): List<Drug>
    suspend fun getDrugById(id: String): Drug?
    suspend fun updateDrug(drug: Drug): Drug
    suspend fun createDrug(drug: Drug): Drug
} 