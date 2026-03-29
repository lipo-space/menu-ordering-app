package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.model.Dish
import com.lipo.menu.data.remote.DishRemoteDataSource
import com.lipo.menu.domain.repository.DishRepository
import com.lipo.menu.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.util.Log
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepositoryImpl @Inject constructor(
    private val dishDao: DishDao,
    private val remoteDataSource: DishRemoteDataSource  // 启用云端同步
) : DishRepository {

    /**
     * 从云端同步数据到本地数据库
     * 在应用启动时调用，拉取所有家庭成员的共享数据
     */
    suspend fun syncFromCloud() {
        try {
            Log.d("DishRepository", "=== Starting sync from cloud ===")
            Log.d("DishRepository", "RemoteDataSource: $remoteDataSource")

            val cloudDishes = remoteDataSource.fetchAllDishes()
            Log.d("DishRepository", "Fetched ${cloudDishes.size} dishes from cloud")

            cloudDishes.forEachIndexed { index, cloudDish ->
                try {
                    Log.d("DishRepository", "Processing dish ${index + 1}/${cloudDishes.size}: ${cloudDish.name}")

                    // 检查本地是否已存在该菜品
                    val localDish = dishDao.getDishByIdSync(cloudDish.id)

                    if (localDish == null) {
                        // 本地不存在，插入
                        dishDao.insertDish(cloudDish.toEntity())
                        Log.d("DishRepository", "✓ Inserted dish from cloud: ${cloudDish.name}")
                    } else {
                        // 本地已存在，比较更新时间，保留最新的
                        if (cloudDish.updatedAt.isAfter(DateUtils.toInstant(localDish.updatedAt))) {
                            dishDao.updateDish(cloudDish.toEntity())
                            Log.d("DishRepository", "✓ Updated dish from cloud: ${cloudDish.name}")
                        } else {
                            Log.d("DishRepository", "- Skipped (local is newer): ${cloudDish.name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DishRepository", "✗ Failed to sync dish ${cloudDish.id}: ${e.message}", e)
                }
            }

            Log.d("DishRepository", "=== Sync completed. Synced ${cloudDishes.size} dishes ===")
        } catch (e: Exception) {
            Log.e("DishRepository", "=== CRITICAL ERROR in syncFromCloud ===", e)
            Log.e("DishRepository", "Error type: ${e::class.simpleName}")
            Log.e("DishRepository", "Error message: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getAllDishes(): Flow<List<Dish>> {
        return dishDao.getAllDishes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchDishes(query: String): Flow<List<Dish>> {
        val searchQuery = if (query.isBlank()) "%" else query.trim()
        return dishDao.searchDishes(searchQuery).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDishById(id: String): Flow<Dish?> {
        return dishDao.getDishById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun addDish(name: String, description: String?): Result<Dish> {
        return try {
            val trimmedName = name.trim()
            val trimmedDescription = description?.trim()

            // Check for duplicate name
            if (dishDao.dishNameExists(trimmedName)) {
                return Result.failure(Exception("Dish with name '$trimmedName' already exists"))
            }

            val now = DateUtils.getCurrentInstant()
            val dish = Dish(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                description = trimmedDescription,
                createdAt = now,
                updatedAt = now,
                isDeleted = false
            )

            // 同步到云端
            remoteDataSource.upsertDish(dish)

            // 保存到本地
            dishDao.insertDish(dish.toEntity())

            Result.success(dish)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDish(id: String, name: String, description: String?): Result<Dish> {
        return try {
            // Check for duplicate name
            if (dishDao.dishNameExists(name, id)) {
                return Result.failure(Exception("Dish with name '$name' already exists"))
            }

            val existingEntity = dishDao.getDishByIdSync(id)
            if (existingEntity == null) {
                return Result.failure(Exception("Dish not found"))
            }

            val updatedDish = Dish(
                id = id,
                name = name.trim(),
                description = description?.trim(),
                createdAt = DateUtils.toInstant(existingEntity.createdAt),
                updatedAt = DateUtils.getCurrentInstant(),
                isDeleted = false
            )

            // 同步到云端
            remoteDataSource.upsertDish(updatedDish)

            // 更新本地
            dishDao.updateDish(updatedDish.toEntity())

            Result.success(updatedDish)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDish(id: String): Result<Unit> {
        return try {
            // 同步到云端
            remoteDataSource.deleteDish(id)

            // 删除本地数据
            dishDao.softDeleteDish(id, DateUtils.getCurrentEpochMilli())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dishNameExists(name: String, excludeId: String?): Boolean {
        return dishDao.dishNameExists(name, excludeId)
    }

    private fun DishEntity.toDomainModel(): Dish {
        return Dish(
            id = this.id,
            name = this.name,
            description = this.description,
            createdAt = DateUtils.toInstant(this.createdAt),
            updatedAt = DateUtils.toInstant(this.updatedAt),
            isDeleted = this.isDeleted
        )
    }

    private fun Dish.toEntity(): DishEntity {
        return DishEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            createdAt = DateUtils.toEpochMilli(this.createdAt),
            updatedAt = DateUtils.toEpochMilli(this.updatedAt),
            isDeleted = this.isDeleted
        )
    }
}
