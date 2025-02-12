package com.example.pharmacist.domain.model

@JvmInline
value class DrugId(val value: String) {
    init {
        require(value.isBlank() || isValidUUID(value)) { 
            "Invalid UUID format: $value" 
        }
    }
    
    companion object {
        private val UUID_REGEX = 
            Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
            
        fun isValidUUID(uuid: String): Boolean = 
            UUID_REGEX.matches(uuid)
    }
} 