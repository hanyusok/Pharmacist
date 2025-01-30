package com.example.pharmacist.di

import io.github.jan.supabase.SupabaseClient as Remote
import com.example.pharmacist.data.SupabaseClient as LocalSupabase
import com.example.pharmacist.data.repository.DrugRepositoryImpl
import com.example.pharmacist.domain.repository.DrugRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    companion object {
        @Provides
        @Singleton
        fun provideSupabaseClient(): Remote {
            return LocalSupabase.client
        }
    }

    @Binds
    @Singleton
    abstract fun bindDrugRepository(
        drugRepositoryImpl: DrugRepositoryImpl
    ): DrugRepository
} 