package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import com.lipo.menu.data.local.database.entities.TodayMenuWithDishes
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.data.remote.TodayMenuRemoteDataSource
import com.lipo.menu.domain.repository.TodayMenuRepository
import com.lipo.menu.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMenuRepositoryImpl @Inject constructor(
    private val todayMenuDao: TodayMenuDao,
    private val remoteDataSource: TodayMenuRemoteDataSource  // 启用云端同步
) : TodayMenuRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * 从云端同步数据到本地数据库
     * 同步 today_menus 和 today_menu_dishes 两张表
     */
    suspend fun syncFromCloud() {
        try {
            Log.d("TodayMenuRepository", "Starting sync from cloud")

            // 1. 同步今日菜单
            val cloudMenus = remoteDataSource.fetchAllTodayMenus()
            cloudMenus.forEach { menuMap ->
                try {
                    val menuId = menuMap["id"] as String
                    val menuDate = menuMap["date"] as String
                    val createdAt = menuMap["created_at"] as String
                    val updatedAt = menuMap["updated_at"] as String

                    // 检查本地是否已存在
                    val localMenu = todayMenuDao.getTodayMenuByIdSync(menuId)

                    if (localMenu == null) {
                        // 本地不存在，插入
                        val entity = TodayMenuEntity(
                            id = menuId,
                            date = menuDate,
                            createdAt = DateUtils.parseISO8601(createdAt).toEpochMilli(),
                            updatedAt = DateUtils.parseISO8601(updatedAt).toEpochMilli()
                        )
                        todayMenuDao.insertTodayMenu(entity)
                        Log.d("TodayMenuRepository", "Inserted today menu from cloud: $menuDate")
                    } else {
                        // 本地已存在，比较更新时间
                        if (DateUtils.parseISO8601(updatedAt).isAfter(
                                DateUtils.toInstant(localMenu.updatedAt)
                            )) {
                            val entity = TodayMenuEntity(
                                id = menuId,
                                date = menuDate,
                                createdAt = DateUtils.parseISO8601(createdAt).toEpochMilli(),
                                updatedAt = DateUtils.parseISO8601(updatedAt).toEpochMilli()
                            )
                            todayMenuDao.updateTodayMenu(entity)
                            Log.d("TodayMenuRepository", "Updated today menu from cloud: $menuDate")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TodayMenuRepository", "Failed to sync today menu: ${e.message}")
                }
            }

            // 2. 同步今日菜单菜品关联
            val cloudDishes = remoteDataSource.fetchAllTodayMenuDishes()
            cloudDishes.forEach { dishMap ->
                try {
                    val todayMenuId = dishMap["today_menu_id"] as String
                    val dishId = dishMap["dish_id"] as String
                    val displayOrder = dishMap["display_order"] as Int

                    // 检查本地是否已存在
                    val localDish = todayMenuDao.getTodayMenuDishByIdsSync(todayMenuId, dishId)

                    if (localDish == null) {
                        // 本地不存在，插入
                        val entity = TodayMenuDishEntity(
                            todayMenuId = todayMenuId,
                            dishId = dishId,
                            displayOrder = displayOrder
                        )
                        todayMenuDao.insertTodayMenuDish(entity)
                        Log.d("TodayMenuRepository", "Inserted today menu dish from cloud: $todayMenuId-$dishId")
                    } else {
                        // 本地已存在，更新 display_order
                        if (displayOrder != localDish.displayOrder) {
                            val entity = TodayMenuDishEntity(
                                todayMenuId = todayMenuId,
                                dishId = dishId,
                                displayOrder = displayOrder
                            )
                            todayMenuDao.updateTodayMenuDish(entity)
                            Log.d("TodayMenuRepository", "Updated today menu dish from cloud: $todayMenuId-$dishId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TodayMenuRepository", "Failed to sync today menu dish: ${e.message}")
                }
            }

            Log.d("TodayMenuRepository", "Sync completed. Synced ${cloudMenus.size} menus and ${cloudDishes.size} dishes")
        } catch (e: Exception) {
            Log.e("TodayMenuRepository", "Failed to sync from cloud: ${e.message}")
        }
    }

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

            // 同步到云端
            val todayMenu = TodayMenu(
                id = menuId,
                date = date,
                dishes = emptyList(),
                createdAt = now,
                updatedAt = now
            )
            remoteDataSource.upsertTodayMenu(todayMenu)

            todayMenuDao.insertTodayMenu(todayMenuEntity)

            // Add dishes to the menu
            dishIds.forEachIndexed { index, dishId ->
                remoteDataSource.upsertTodayMenuDish(menuId, dishId, index)

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

            // 同步到云端
            val existingMenu = todayMenuDao.getTodayMenuById(id).first()
            if (existingMenu != null) {
                val todayMenu = TodayMenu(
                    id = id,
                    date = date,
                    dishes = emptyList(),
                    createdAt = DateUtils.toInstant(existingMenu.todayMenu.createdAt),
                    updatedAt = now
                )
                remoteDataSource.upsertTodayMenu(todayMenu)

                // 删除云端的旧菜品关联
                remoteDataSource.deleteTodayMenuDishesByMenu(id)
            }

            todayMenuDao.updateTodayMenu(
                id = id,
                date = date.format(dateFormatter),
                updatedAt = DateUtils.toEpochMilli(now)
            )

            // Delete existing dishes and add new ones
            todayMenuDao.deleteTodayMenuDishesByMenu(id)
            dishIds.forEachIndexed { index, dishId ->
                // 同步到云端
                remoteDataSource.upsertTodayMenuDish(id, dishId, index)

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
            // 同步到云端
            remoteDataSource.deleteTodayMenuDishesByMenu(id)
            remoteDataSource.deleteTodayMenu(id)

            todayMenuDao.deleteTodayMenu(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDishToTodayMenu(todayMenuId: String, dishId: String, order: Int): Result<Unit> {
        return try {
            // 同步到云端
            remoteDataSource.upsertTodayMenuDish(todayMenuId, dishId, order)

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
            // 同步到云端
            remoteDataSource.deleteTodayMenuDish(todayMenuId, dishId)

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
