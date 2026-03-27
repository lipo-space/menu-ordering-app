package com.lipo.menu.domain.repository

import com.lipo.menu.data.model.TodayMenu
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TodayMenuRepository {
    fun getTodayMenu(date: LocalDate): Flow<TodayMenu?>
    fun getTodayMenuById(id: String): Flow<TodayMenu?>
    fun getHistoricalMenus(): Flow<List<TodayMenu>>
    fun getMenusByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodayMenu>>
    fun searchHistoricalMenus(query: String): Flow<List<TodayMenu>>
    suspend fun createTodayMenu(date: LocalDate, dishIds: List<String>): Result<TodayMenu>
    suspend fun updateTodayMenu(id: String, date: LocalDate, dishIds: List<String>): Result<TodayMenu>
    suspend fun deleteTodayMenu(id: String): Result<Unit>
    suspend fun addDishToTodayMenu(todayMenuId: String, dishId: String, order: Int): Result<Unit>
    suspend fun removeDishFromTodayMenu(todayMenuId: String, dishId: String): Result<Unit>
    fun getUsageCountForDish(dishId: String): Flow<Int>
}
