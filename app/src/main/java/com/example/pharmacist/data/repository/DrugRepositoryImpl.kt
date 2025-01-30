package com.example.pharmacist.data.repository

import android.util.Log
import com.example.pharmacist.data.dto.DrugDto
import com.example.pharmacist.data.mapper.toDrug
import com.example.pharmacist.domain.model.Drug
import com.example.pharmacist.domain.repository.DrugRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
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
            val response = client.postgrest["drugs"]
                .select {
                    filter {
                        eq("drug_name", trimmedQuery)
                    }
//                    textSearch(
//                        column = "drug_name",
//                        query = trimmedQuery,
//                        type = TextSearchType.PLAIN
//                    )
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

    // Add this function to test the connection and queries
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["drugs"]
                .select {
                    limit(1)
                }
            val result = response.decodeList<DrugDto>()
            Log.d("DrugRepositoryImpl", 
                "Connection test successful, found ${result.size} drug")
            true
        } catch (e: Exception) {
            Log.e("DrugRepositoryImpl", 
                "Connection test failed", e)
            false
        }
    }
} 