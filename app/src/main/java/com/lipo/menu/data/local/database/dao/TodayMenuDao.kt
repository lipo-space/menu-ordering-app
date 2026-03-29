package com.lipo.menu.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import com.lipo.menu.data.local.database.entities.TodayMenuWithDishes
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayMenuDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTodayMenu(todayMenu: TodayMenuEntity)

    @Query("UPDATE today_menus SET date = :date, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTodayMenu(id: String, date: String, updatedAt: Long)

    @Query("DELETE FROM today_menus WHERE id = :id")
    suspend fun deleteTodayMenu(id: String)

    @Transaction
    @Query("SELECT * FROM today_menus WHERE date = :date")
    fun getTodayMenuByDate(date: String): Flow<TodayMenuWithDishes?>

    @Transaction
    @Query("SELECT * FROM today_menus WHERE id = :id")
    fun getTodayMenuById(id: String): Flow<TodayMenuWithDishes?>

    @Transaction
    @Query("SELECT * FROM today_menus ORDER BY date DESC")
    fun getAllHistoricalMenus(): Flow<List<TodayMenuWithDishes>>

    @Transaction
    @Query("SELECT * FROM today_menus WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getMenusByDateRange(startDate: String, endDate: String): Flow<List<TodayMenuWithDishes>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT tm.* FROM today_menus tm
        INNER JOIN today_menu_dishes tmd ON tm.id = tmd.today_menu_id
        INNER JOIN dishes d ON tmd.dish_id = d.id
        WHERE LOWER(d.name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY tm.date DESC
        """
    )
    fun searchMenusByDishName(query: String): Flow<List<TodayMenuWithDishes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayMenuDish(todayMenuDish: TodayMenuDishEntity)

    @Query("DELETE FROM today_menu_dishes WHERE today_menu_id = :todayMenuId AND dish_id = :dishId")
    suspend fun deleteTodayMenuDish(todayMenuId: String, dishId: String)

    @Query("DELETE FROM today_menu_dishes WHERE today_menu_id = :todayMenuId")
    suspend fun deleteTodayMenuDishesByMenu(todayMenuId: String)

    @Query("SELECT COUNT(*) FROM today_menu_dishes WHERE dish_id = :dishId")
    fun getUsageCountForDish(dishId: String): Flow<Int>

    @Query("SELECT * FROM today_menus WHERE id = :id LIMIT 1")
    suspend fun getTodayMenuByIdSync(id: String): TodayMenuEntity?

    @Query("SELECT * FROM today_menu_dishes WHERE today_menu_id = :todayMenuId AND dish_id = :dishId LIMIT 1")
    suspend fun getTodayMenuDishByIdsSync(todayMenuId: String, dishId: String): TodayMenuDishEntity?

    @Query("UPDATE today_menu_dishes SET `order` = :order WHERE today_menu_id = :todayMenuId AND dish_id = :dishId")
    suspend fun updateTodayMenuDish(todayMenuId: String, dishId: String, order: Int)
}
