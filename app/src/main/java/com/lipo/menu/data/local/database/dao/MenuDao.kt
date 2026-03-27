package com.lipo.menu.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lipo.menu.data.local.database.entities.MenuEntity
import com.lipo.menu.data.local.database.entities.MenuEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menus ORDER BY date DESC")
    fun getAllMenus(): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menus WHERE date = :date")
    fun getMenuByDate(date: Long): Flow<MenuEntity?>

    @Query("SELECT * FROM menus WHERE id = :id")
    fun getMenuById(id: String): Flow<MenuEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMenu(menu: MenuEntity)

    @Query("DELETE FROM menus WHERE id = :id")
    suspend fun deleteMenu(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuEntry(entry: MenuEntryEntity)

    @Query("DELETE FROM menu_entries WHERE menu_id = :menuId AND dish_id = :dishId AND `order` = :order")
    suspend fun deleteMenuEntry(menuId: String, dishId: String, order: Int)

    @Query("SELECT * FROM menu_entries WHERE menu_id = :menuId ORDER BY `order` ASC")
    fun getMenuEntries(menuId: String): Flow<List<MenuEntryEntity>>
}
