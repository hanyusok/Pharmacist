package com.example.pharmacist.data.repository


//import com.example.pharmacist.data.SupabaseClient
import android.util.Log
import com.example.pharmacist.data.dto.DrugDto
import com.example.pharmacist.data.mapper.toDrug
import com.example.pharmacist.data.mapper.toDto
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.repository.DrugRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrugRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : DrugRepository {

    init {
        Log.d("DrugRepositoryImpl", "Repository initialized")
    }

    override suspend fun getDrugs(): List<Drug> = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting getDrugs() request")
            
            val response = client.postgrest["drugs"]
                .select(columns = Columns.list(
                    "id",
                    "main_code",
                    "ingredient",
                    "drug_code",
                    "drug_name",
                    "manufacturer",
                    "covered_by_insurance"
                )) {
                    limit(100)
                }
            
            Log.d("DrugRepositoryImpl", "Raw response received")
            
            val drugDtos = response.decodeList<DrugDto>()
            Log.d("DrugRepositoryImpl", "Decoded ${drugDtos.size} DrugDtos")
            
            // Log first item for debugging
            if (drugDtos.isNotEmpty()) {
                Log.d("DrugRepositoryImpl", "First drug: ${drugDtos.first()}")
            }
            
            drugDtos.map { it.toDrug() }.also { drugs ->
                Log.d("DrugRepositoryImpl", "Successfully mapped ${drugs.size} drugs")
                if (drugs.isNotEmpty()) {
                    Log.d("DrugRepositoryImpl", "First mapped drug: ${drugs.first()}")
                }
            }
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error in getDrugs()", e)
            Log.e("DrugRepositoryImpl", "Stack trace: ${e.stackTraceToString()}")
            emptyList()
        }
    }

    override suspend fun searchDrugs(query: String): List<Drug> = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Searching drugs with query: $query")

            if (query.isBlank()) {
                Log.d("DrugRepositoryImpl", "Empty query, returning all drugs")
                return@withContext getDrugs()
            }

            val trimmedQuery = query.trim()
            val response = client.postgrest["drugs"]
                .select(columns = Columns.list(
                    "id",
                    "main_code",
                    "ingredient",
                    "drug_code",
                    "drug_name",
                    "manufacturer",
                    "covered_by_insurance"
                )) {
                    filter {
                        ilike("drug_name", "%$trimmedQuery%")
                    }
                    limit(50)
                }
                

            response.decodeList<DrugDto>().also { drugs ->
                Log.d("DrugRepositoryImpl", 
                    "Search results for '$query': found ${drugs.size} drugs")
            }.map { it.toDrug() }
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", 
                "Error searching drugs with query: '$query'", e)
            emptyList()
        }
    }

    override suspend fun getDrugById(id: String): Drug? = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Fetching drug with id: $id")
            
            val response = client.postgrest["drugs"]
                .select(columns = Columns.list(
                    "id",
                    "main_code",
                    "ingredient",
                    "drug_code",
                    "drug_name",
                    "manufacturer",
                    "covered_by_insurance"
                )) {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<DrugDto>()
                ?.let { dto ->
                    Log.d("DrugRepositoryImpl", "Found drug: ${dto.drug_name}")
                    dto.toDrug().also { drug ->
                        if (drug.id != id) {
                            Log.w("DrugRepositoryImpl", "ID mismatch: expected $id, got ${drug.id}")
                        }
                    }
                }
            
            response
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error fetching drug with id: $id", e)
            null
        }
    }

    override suspend fun updateDrug(drug: Drug): Drug = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting update operation for drug ID: ${drug.id}")
            Log.d("DrugRepositoryImpl", "Update payload: $drug")
            
            requireNotNull(drug.id) { "Drug ID cannot be null for update operation" }
            
            // First verify the drug exists
            val existingDrug = getDrugById(drug.id)
            requireNotNull(existingDrug) { "Drug with id ${drug.id} not found" }
            
            val drugDto = drug.toDto()
            
            // Add more detailed logging before the update
            Log.d("DrugRepositoryImpl", "Sending update request to Supabase")
            Log.d("DrugRepositoryImpl", "DTO to be sent: $drugDto")

            try {
                client.postgrest["drugs"]
                    .update({
                        set("main_code", drugDto.main_code)
                        set("drug_name", drugDto.drug_name)
                        set("ingredient", drugDto.ingredient)
                        set("drug_code", drugDto.drug_code)
                        set("manufacturer", drugDto.manufacturer)
                        set("covered_by_insurance", drugDto.covered_by_insurance)
                    }) {
                        filter {
                            eq("id", drug.id)
                        }
                    }

                Log.d("DrugRepositoryImpl", "Update successful")
                return@withContext drug

            } catch (e: Exception) {
                Log.e("DrugRepositoryImpl", "Update failed with error", e)
                Log.e("DrugRepositoryImpl", "Error type: ${e.javaClass.simpleName}")
                Log.e("DrugRepositoryImpl", "Error message: ${e.message}")
                throw IllegalStateException("Failed to update drug: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error updating drug: ${e.message}")
            Log.e("DrugRepositoryImpl", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun createDrug(drug: Drug): Drug = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting create operation")
            Log.d("DrugRepositoryImpl", "Create payload: $drug")
            
            val drugDto = drug.toDto()
            
            client.postgrest["drugs"]
                .insert(drugDto)

            Log.d("DrugRepositoryImpl", "Drug created successfully")
            return@withContext drug

        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Create failed with error", e)
            throw IllegalStateException("Failed to create drug: ${e.message}", e)
        }
    }

    override suspend fun deleteDrug(drugId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting delete operation for drug: $drugId")
            
            client.postgrest["drugs"]
                .delete {
                    filter {
                        eq("id", drugId)
                    }
                }
            
            Log.d("DrugRepositoryImpl", "Drug deleted successfully")
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Delete failed with error", e)
            throw IllegalStateException("Failed to delete drug: ${e.message}", e)
        }
    }
} 