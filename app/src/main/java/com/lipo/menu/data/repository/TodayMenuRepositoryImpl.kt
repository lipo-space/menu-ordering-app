package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import com.lipo.menu.data.local.database.entities.TodayMenuWithDishes
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import com.lipo.menu.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMenuRepositoryImpl @Inject constructor(
    private val todayMenuDao: TodayMenuDao
) : TodayMenuRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getTodayMenu(date: LocalDate): Flow<TodayMenu?> {
        return todayMenuDao.getTodayMenuByDate(date.format(dateFormatter)).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getTodayMenuById(id: String): Flow<TodayMenu?> {
        return todayMenuDao.getTodayMenuById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getHistoricalMenus(): Flow<List<TodayMenu>> {
        return todayMenuDao.getAllHistoricalMenus().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getMenusByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodayMenu>> {
        return todayMenuDao.getMenusByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchHistoricalMenus(query: String): Flow<List<TodayMenu>> {
        val searchQuery = if (query.isBlank()) "%" else "%${query.trim()}%"
        return todayMenuDao.searchMenusByDishName(searchQuery).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createTodayMenu(date: LocalDate, dishIds: List<String>): Result<TodayMenu> {
        return try {
            val now = DateUtils.getCurrentInstant()
            val menuId = UUID.randomUUID().toString()

            val todayMenuEntity = TodayMenuEntity(
                id = menuId,
                date = date.format(dateFormatter),
                createdAt = DateUtils.toEpochMilli(now),
                updatedAt = DateUtils.toEpochMilli(now)
            )

            todayMenuDao.insertTodayMenu(todayMenuEntity)

            // Add dishes to the menu
            dishIds.forEachIndexed { index, dishId ->
                todayMenuDao.insertTodayMenuDish(
                    TodayMenuDishEntity(
                        todayMenuId = menuId,
                        dishId = dishId,
                        order = index
                    )
                )
            }

            // Fetch the created menu with dishes
            val createdMenu = todayMenuDao.getTodayMenuById(menuId).map { it?.toDomainModel() }.first()
            Result.success(createdMenu!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTodayMenu(id: String, date: LocalDate, dishIds: List<String>): Result<TodayMenu> {
        return try {
            val now = DateUtils.getCurrentInstant()

            todayMenuDao.updateTodayMenu(
                id = id,
                date = date.format(dateFormatter),
                updatedAt = DateUtils.toEpochMilli(now)
            )

            // Delete existing dishes and add new ones
            todayMenuDao.deleteTodayMenuDishesByMenu(id)
            dishIds.forEachIndexed { index, dishId ->
                todayMenuDao.insertTodayMenuDish(
                    TodayMenuDishEntity(
                        todayMenuId = id,
                        dishId = dishId,
                        order = index
                    )
                )
            }

            // Fetch the updated menu with dishes
            val updatedMenu = todayMenuDao.getTodayMenuById(id).map { it?.toDomainModel() }.first()
            Result.success(updatedMenu!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTodayMenu(id: String): Result<Unit> {
        return try {
            todayMenuDao.deleteTodayMenu(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDishToTodayMenu(todayMenuId: String, dishId: String, order: Int): Result<Unit> {
        return try {
            todayMenuDao.insertTodayMenuDish(
                TodayMenuDishEntity(
                    todayMenuId = todayMenuId,
                    dishId = dishId,
                    order = order
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeDishFromTodayMenu(todayMenuId: String, dishId: String): Result<Unit> {
        return try {
            todayMenuDao.deleteTodayMenuDish(todayMenuId, dishId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUsageCountForDish(dishId: String): Flow<Int> {
        return todayMenuDao.getUsageCountForDish(dishId)
    }

    private fun TodayMenuWithDishes.toDomainModel(): TodayMenu {
        return TodayMenu(
            id = this.todayMenu.id,
            date = LocalDate.parse(this.todayMenu.date, dateFormatter),
            dishes = this.dishes
                .filter { !it.isDeleted } // Filter out soft-deleted dishes
                .map { dishEntity ->
                    dishEntity.toDomainModel()
                },
            createdAt = DateUtils.toInstant(this.todayMenu.createdAt),
            updatedAt = DateUtils.toInstant(this.todayMenu.updatedAt)
        )
    }

    private fun TodayMenuEntity.toDomainModel(dishes: List<com.lipo.menu.data.model.Dish> = emptyList()): TodayMenu {
        return TodayMenu(
            id = this.id,
            date = LocalDate.parse(this.date, dateFormatter),
            dishes = dishes,
            createdAt = DateUtils.toInstant(this.createdAt),
            updatedAt = DateUtils.toInstant(this.updatedAt)
        )
    }

    private fun com.lipo.menu.data.local.database.entities.DishEntity.toDomainModel(): com.lipo.menu.data.model.Dish {
        return com.lipo.menu.data.model.Dish(
            id = this.id,
            name = this.name,
            description = this.description,
            createdAt = DateUtils.toInstant(this.createdAt),
            updatedAt = DateUtils.toInstant(this.updatedAt),
            isDeleted = this.isDeleted
        )
    }
}
