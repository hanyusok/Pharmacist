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
import com.example.pharmacist.domain.model.DrugId

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

    override suspend fun getDrugById(id: DrugId): Drug? = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Fetching drug with id: ${id.value}")
            
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
                        eq("id", id.value)
                    }
                }
                .decodeSingleOrNull<DrugDto>()
                ?.let { dto ->
                    Log.d("DrugRepositoryImpl", "Found drug: ${dto.drug_name}")
                    dto.toDrug()
                }
            
            response
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error fetching drug with id: ${id.value}", e)
            null
        }
    }

    override suspend fun updateDrug(drug: Drug): Drug = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting update operation for drug ID: ${drug.id.value}")
            Log.d("DrugRepositoryImpl", "Update payload: $drug")
            
            // First verify the drug exists
            val existingDrug = getDrugById(drug.id)
            requireNotNull(existingDrug) { "Drug with id ${drug.id.value} not found" }
            
            val drugDto = drug.toDto()
            
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
                        eq("id", drug.id.value)
                    }
                }

            Log.d("DrugRepositoryImpl", "Update successful")
            return@withContext drug

        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Update failed with error", e)
            throw e
        }
    }

    override suspend fun createDrug(drug: Drug): Drug = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting create operation")
            Log.d("DrugRepositoryImpl", "Create payload: $drug")
            
            val drugDto = drug.toDto()
            
            // The response will contain the created drug with the UUID
            val response = client.postgrest["drugs"]
                .insert(drugDto) { select() }  // Add select() to get the response
                .decodeSingle<DrugDto>()  // Decode the response
            
            Log.d("DrugRepositoryImpl", "Drug created successfully with ID: ${response.id}")
            return@withContext response.toDrug()

        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Create failed with error", e)
            throw IllegalStateException("Failed to create drug: ${e.message}", e)
        }
    }

    override suspend fun deleteDrug(drugId: DrugId): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Starting delete operation for drug: ${drugId.value}")
            
            client.postgrest["drugs"]
                .delete {
                    filter {
                        eq("id", drugId.value)
                    }
                }
            
            Log.d("DrugRepositoryImpl", "Drug deleted successfully")
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Delete failed with error", e)
            throw IllegalStateException("Failed to delete drug: ${e.message}", e)
        }
    }
} 