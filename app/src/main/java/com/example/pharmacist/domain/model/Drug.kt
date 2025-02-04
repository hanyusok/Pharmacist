package com.example.pharmacist.domain.model

data class Drug(
    val id: String,
    val mainCode: String,
    val ingredient: String,
    val drugCode: String,
    val drugName: String,
    val manufacturer: String,
    val isCoveredByInsurance: Boolean
)