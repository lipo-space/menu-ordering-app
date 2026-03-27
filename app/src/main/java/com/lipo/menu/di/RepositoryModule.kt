package com.lipo.menu.di

import com.lipo.menu.data.repository.CombinationRepositoryImpl
import com.lipo.menu.data.repository.DishRepositoryImpl
import com.lipo.menu.data.repository.TodayMenuRepositoryImpl
import com.lipo.menu.domain.repository.CombinationRepository
import com.lipo.menu.domain.repository.DishRepository
import com.lipo.menu.domain.repository.TodayMenuRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDishRepository(
        dishRepositoryImpl: DishRepositoryImpl
    ): DishRepository

    @Binds
    @Singleton
    abstract fun bindCombinationRepository(
        combinationRepositoryImpl: CombinationRepositoryImpl
    ): CombinationRepository

    @Binds
    @Singleton
    abstract fun bindTodayMenuRepository(
        todayMenuRepositoryImpl: TodayMenuRepositoryImpl
    ): TodayMenuRepository
}
