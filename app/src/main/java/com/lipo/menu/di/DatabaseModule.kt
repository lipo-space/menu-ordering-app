package com.lipo.menu.di

import android.content.Context
import androidx.room.Room
import com.lipo.menu.data.local.database.MenuDatabase
import com.lipo.menu.data.local.database.dao.CombinationDao
import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.dao.MenuDao
import com.lipo.menu.data.local.database.dao.SyncDao
import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.migrations.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MenuDatabase {
        return Room.databaseBuilder(
            context,
            MenuDatabase::class.java,
            "menu_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDishDao(database: MenuDatabase): DishDao {
        return database.dishDao()
    }

    @Provides
    fun provideMenuDao(database: MenuDatabase): MenuDao {
        return database.menuDao()
    }

    @Provides
    fun provideCombinationDao(database: MenuDatabase): CombinationDao {
        return database.combinationDao()
    }

    @Provides
    fun provideSyncDao(database: MenuDatabase): SyncDao {
        return database.syncDao()
    }

    @Provides
    fun provideTodayMenuDao(database: MenuDatabase): TodayMenuDao {
        return database.todayMenuDao()
    }
}
