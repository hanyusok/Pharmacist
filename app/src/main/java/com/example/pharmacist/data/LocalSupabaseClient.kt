package com.example.pharmacist.data

import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

class LocalSupabaseClient @Inject constructor(
    val client: SupabaseClient
) 