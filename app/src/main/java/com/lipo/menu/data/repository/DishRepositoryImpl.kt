package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import com.lipo.menu.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepositoryImpl @Inject constructor(
    private val dishDao: DishDao
) : DishRepository {

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
            // Check for duplicate name
            if (dishDao.dishNameExists(name)) {
                return Result.failure(Exception("Dish with name '$name' already exists"))
            }

            val now = DateUtils.getCurrentInstant()
            val dish = Dish(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description?.trim(),
                createdAt = now,
                updatedAt = now,
                isDeleted = false
            )

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

            dishDao.updateDish(updatedDish.toEntity())
            Result.success(updatedDish)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDish(id: String): Result<Unit> {
        return try {
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
