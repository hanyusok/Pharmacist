package com.example.pharmacist.di

import com.example.pharmacist.data.repository.DrugRepositoryImpl
import com.example.pharmacist.domain.repository.DrugRepository
import com.example.pharmacist.data.repository.OrderRepositoryImpl
import com.example.pharmacist.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindDrugRepository(
        drugRepositoryImpl: DrugRepositoryImpl
    ): DrugRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
} 