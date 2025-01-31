package com.example.pharmacist.data.repository

import android.service.autofill.Validators.or
import android.util.Log
import com.example.pharmacist.data.dto.DrugDto
import com.example.pharmacist.data.mapper.toDrug
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.repository.DrugRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
//import io.github.jan.supabase.postgrest.query.filter.FilterOperator
//import io.github.jan.supabase.postgrest.query.filter.PostgrestFilter
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
            val response = client.postgrest["drugs"].select()
            response.decodeList<DrugDto>().also { 
                Log.d("DrugRepositoryImpl", "Fetched ${it.size} drugs")
            }.map { it.toDrug() }
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error fetching drugs", e)
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
            val response = client.postgrest.from("drugs")
                .select(columns = Columns.list("drug_name")) {
                    filter {
                        Drug::drugName ilike "%$trimmedQuery%"
//                        ilike("drug_name", "%$trimmedQuery%")
//                        ilike("ingredient", "%$trimmedQuery%")
//                        ilike("drug_code", "%$trimmedQuery%")
                    }
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

    override suspend fun getDrugById(id: Long): Drug? = withContext(Dispatchers.IO) {
        try {
            Log.d("DrugRepositoryImpl", "Fetching drug with id: $id")
            
            val response = client.postgrest.from("drugs")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("id", "$id")
                        Log.d("DrugRepositoryImpl", "Filter condition: $id")
                    }
                }

            // drug fetching object transfer error !!!! //
            response.decodeSingleOrNull<DrugDto>()?.let { dto ->
                Log.d("DrugRepositoryImpl", "Found drug: ${dto.drug_name}")
                dto.toDrug()
            }.also { drug ->
                Log.d("DrugRepositoryImpl", "Mapped to domain model: ${drug?.drugName}")
            }
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", "Error fetching drug with id: $id", e)
            null
        }
    }
} 