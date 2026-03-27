package com.lipo.menu.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.dao.MenuDao
import com.lipo.menu.data.local.database.dao.CombinationDao
import com.lipo.menu.data.local.database.dao.SyncDao
import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.entities.CombinationDishEntity
import com.lipo.menu.data.local.database.entities.CombinationEntity
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.local.database.entities.MenuEntity
import com.lipo.menu.data.local.database.entities.MenuEntryEntity
import com.lipo.menu.data.local.database.entities.SyncStatusEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity

@Database(
    entities = [
        DishEntity::class,
        MenuEntity::class,
        MenuEntryEntity::class,
        CombinationEntity::class,
        CombinationDishEntity::class,
        SyncStatusEntity::class,
        TodayMenuEntity::class,
        TodayMenuDishEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun dishDao(): DishDao
    abstract fun menuDao(): MenuDao
    abstract fun combinationDao(): CombinationDao
    abstract fun syncDao(): SyncDao
    abstract fun todayMenuDao(): TodayMenuDao
}
