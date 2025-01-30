package com.example.pharmacist.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DrugDto(
    val id: Long,
    val main_code: String,
    val ingredient: String,
    val drug_code: String,
    val drug_name: String,
    val manufacturer: String,
    val covered_by_insurance: String
) 