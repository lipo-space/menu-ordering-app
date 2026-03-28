package com.lipo.menu.di

import com.lipo.menu.data.remote.DishRemoteDataSource
import com.lipo.menu.data.remote.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Supabase 依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig {
        return SupabaseConfig()
    }

    @Provides
    @Singleton
    fun provideDishRemoteDataSource(
        supabaseConfig: SupabaseConfig
    ): DishRemoteDataSource {
        return DishRemoteDataSource(supabaseConfig)
    }
}
