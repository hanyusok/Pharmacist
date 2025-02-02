package com.example.pharmacist.data.mapper

import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.data.dto.DrugDto

fun DrugDto.toDrug(): Drug {
    return Drug(
        id = id,
        mainCode = main_code,
        ingredient = ingredient,
        drugCode = drug_code,
        drugName = drug_name,
        manufacturer = manufacturer,
        isCoveredByInsurance = covered_by_insurance
    )
}

fun Drug.toDto(): DrugDto {
    return DrugDto(
        id = id,
        main_code = mainCode,
        ingredient = ingredient,
        drug_code = drugCode,
        drug_name = drugName,
        manufacturer = manufacturer,
        covered_by_insurance = isCoveredByInsurance
    )
} 