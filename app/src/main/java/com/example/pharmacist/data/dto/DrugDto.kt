package com.example.pharmacist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
//@SerialName("DrugDto")
data class DrugDto(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("main_code")
    val main_code: String = "",
    @SerialName("ingredient")
    val ingredient: String = "",
    @SerialName("drug_code")
    val drug_code: String = "",
    @SerialName("drug_name")
    val drug_name: String = "",
    @SerialName("manufacturer")
    val manufacturer: String = "",
    @SerialName("covered_by_insurance")
    val covered_by_insurance: String = ""
)