package com.lipo.menu.di

// TODO: Supabase 模块暂时禁用
// 等待依赖问题解决后启用

/*
import com.lipo.menu.data.remote.DishRemoteDataSource
import com.lipo.menu.data.remote.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
*/

/**
 * Supabase 依赖注入模块
 * 暂时禁用，等待依赖问题解决
 */
/*
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
*/
