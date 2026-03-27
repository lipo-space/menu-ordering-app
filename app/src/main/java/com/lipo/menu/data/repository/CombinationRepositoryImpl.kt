package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.CombinationDao
import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.entities.CombinationDishEntity
import com.lipo.menu.data.local.database.entities.CombinationEntity
import com.lipo.menu.data.local.database.entities.CombinationWithDishes
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.model.Combination
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.CombinationRepository
import com.lipo.menu.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinationRepositoryImpl @Inject constructor(
    private val combinationDao: CombinationDao,
    private val dishDao: DishDao
) : CombinationRepository {

    override fun getAllCombinations(): Flow<List<Combination>> {
        return combinationDao.getAllCombinationsWithDishes().map { combinations ->
            combinations.map { it.toDomainModel() }
        }
    }

    override fun searchCombinations(query: String): Flow<List<Combination>> {
        val searchQuery = if (query.isBlank()) "%" else "%${query.trim()}%"
        return combinationDao.searchCombinationsWithDishesByName(searchQuery).map { combinations ->
            combinations.map { it.toDomainModel() }
        }
    }

    override fun getCombinationById(id: String): Flow<Combination?> {
        return combinationDao.getCombinationWithDishesById(id).map { combinationWithDishes ->
            combinationWithDishes?.toDomainModel()
        }
    }

    override suspend fun createCombination(
        name: String,
        description: String?,
        dishIds: List<String>
    ): Result<Combination> {
        return try {
            // Validate at least one dish
            if (dishIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("At least one dish must be selected"))
            }

            // Check for duplicate name
            if (combinationDao.combinationNameExists(name)) {
                return Result.failure(IllegalArgumentException("Combination with name '$name' already exists"))
            }

            // Verify all dishes exist and are not deleted
            val dishes = mutableListOf<Dish>()
            for (dishId in dishIds) {
                val dishEntity = dishDao.getDishByIdSync(dishId)
                if (dishEntity == null || dishEntity.isDeleted) {
                    return Result.failure(IllegalArgumentException("Dish with ID '$dishId' not found or has been deleted"))
                }
                dishes.add(dishEntity.toDomainModel())
            }

            val now = DateUtils.getCurrentInstant()
            val combinationId = UUID.randomUUID().toString()
            val combination = Combination(
                id = combinationId,
                name = name.trim(),
                description = description?.trim(),
                createdAt = now,
                updatedAt = now,
                dishes = dishes
            )

            // Insert combination
            combinationDao.insertCombination(combination.toEntity())

            // Insert dish associations
            dishIds.forEachIndexed { index, dishId ->
                combinationDao.insertCombinationDish(
                    CombinationDishEntity(
                        combinationId = combinationId,
                        dishId = dishId,
                        order = index
                    )
                )
            }

            Result.success(combination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCombination(
        id: String,
        name: String,
        description: String?
    ): Result<Combination> {
        return try {
            // Check for duplicate name
            if (combinationDao.combinationNameExists(name, id)) {
                return Result.failure(IllegalArgumentException("Combination with name '$name' already exists"))
            }

            // Get existing combination
            val existingWithDishes = combinationDao.getCombinationWithDishesById(id).first()
            if (existingWithDishes == null) {
                return Result.failure(IllegalArgumentException("Combination not found"))
            }

            val updatedCombination = Combination(
                id = id,
                name = name.trim(),
                description = description?.trim(),
                createdAt = DateUtils.toInstant(existingWithDishes.combination.createdAt),
                updatedAt = DateUtils.getCurrentInstant(),
                dishes = existingWithDishes.dishes.map { it.toDomainModel() }
            )

            combinationDao.updateCombination(
                id = id,
                name = name.trim(),
                description = description?.trim(),
                updatedAt = DateUtils.toEpochMilli(updatedCombination.updatedAt)
            )

            Result.success(updatedCombination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCombination(id: String): Result<Unit> {
        return try {
            // Note: Cascade delete will automatically remove dish associations
            combinationDao.deleteCombination(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDishToCombination(combinationId: String, dishId: String): Result<Unit> {
        return try {
            // Check if dish exists and is not deleted
            val dishEntity = dishDao.getDishByIdSync(dishId)
            if (dishEntity == null || dishEntity.isDeleted) {
                return Result.failure(IllegalArgumentException("Dish not found or has been deleted"))
            }

            // Check if combination exists
            val combination = combinationDao.getCombinationWithDishesById(combinationId).first()
            if (combination == null) {
                return Result.failure(IllegalArgumentException("Combination not found"))
            }

            // Check for duplicate
            if (combination.dishes.any { it.id == dishId }) {
                return Result.failure(IllegalArgumentException("Dish is already in this combination"))
            }

            // Get the next order number
            val currentDishes = combinationDao.getCombinationDishes(combinationId).first()
            val nextOrder = if (currentDishes.isEmpty()) 0 else (currentDishes.maxOf { it.order } + 1)

            combinationDao.insertCombinationDish(
                CombinationDishEntity(
                    combinationId = combinationId,
                    dishId = dishId,
                    order = nextOrder
                )
            )

            // Update combination's updated_at timestamp
            val now = DateUtils.getCurrentEpochMilli()
            combinationDao.updateCombination(
                id = combinationId,
                name = combination.combination.name,
                description = combination.combination.description,
                updatedAt = now
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeDishFromCombination(combinationId: String, dishId: String): Result<Unit> {
        return try {
            // Check if combination exists
            val combination = combinationDao.getCombinationWithDishesById(combinationId).first()
            if (combination == null) {
                return Result.failure(IllegalArgumentException("Combination not found"))
            }

            // Check if this is the last dish
            if (combination.dishes.size <= 1) {
                return Result.failure(
                    IllegalArgumentException(
                        "Cannot remove the last dish from combination. Consider deleting the combination instead."
                    )
                )
            }

            combinationDao.deleteCombinationDish(combinationId, dishId)

            // Update combination's updated_at timestamp
            val now = DateUtils.getCurrentEpochMilli()
            combinationDao.updateCombination(
                id = combinationId,
                name = combination.combination.name,
                description = combination.combination.description,
                updatedAt = now
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun combinationNameExists(name: String, excludeId: String?): Boolean {
        return combinationDao.combinationNameExists(name, excludeId)
    }

    override fun getCombinationCountForDish(dishId: String): Flow<Int> {
        return combinationDao.getCombinationCountForDish(dishId)
    }

    // Mapper functions
    private fun CombinationWithDishes.toDomainModel(): Combination {
        return Combination(
            id = this.combination.id,
            name = this.combination.name,
            description = this.combination.description,
            createdAt = DateUtils.toInstant(this.combination.createdAt),
            updatedAt = DateUtils.toInstant(this.combination.updatedAt),
            dishes = this.dishes.map { it.toDomainModel() }
        )
    }

    private fun CombinationEntity.toDomainModel(): Combination {
        return Combination(
            id = this.id,
            name = this.name,
            description = this.description,
            createdAt = DateUtils.toInstant(this.createdAt),
            updatedAt = DateUtils.toInstant(this.updatedAt),
            dishes = emptyList() // Dishes need to be loaded separately
        )
    }

    private fun Combination.toEntity(): CombinationEntity {
        return CombinationEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            createdAt = DateUtils.toEpochMilli(this.createdAt),
            updatedAt = DateUtils.toEpochMilli(this.updatedAt)
        )
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
}
