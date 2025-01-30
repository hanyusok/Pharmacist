package com.example.pharmacist.domain.model

data class Drug(
    val id: Long,
    val mainCode: String,
    val ingredient: String,
    val drugCode: String,
    val drugName: String,
    val manufacturer: String,
    val isCoveredByInsurance: Boolean
)