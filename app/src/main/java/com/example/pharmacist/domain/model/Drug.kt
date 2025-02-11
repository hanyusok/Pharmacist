package com.example.pharmacist.domain.model

data class Drug(
    val id: DrugId,
    val mainCode: String,
    val ingredient: String,
    val drugCode: String,
    val drugName: String,
    val manufacturer: String,
    val isCoveredByInsurance: Boolean
) {
    companion object {
        fun empty() = Drug(
            id = DrugId(""),
            mainCode = "",
            drugName = "",
            ingredient = "",
            drugCode = "",
            manufacturer = "",
            isCoveredByInsurance = false
        )
    }
}