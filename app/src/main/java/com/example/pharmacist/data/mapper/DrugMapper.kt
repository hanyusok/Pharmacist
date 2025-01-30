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
        isCoveredByInsurance = covered_by_insurance == "Y"
    )
} 