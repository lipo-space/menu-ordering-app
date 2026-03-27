package com.lipo.menu.domain.repository

import com.lipo.menu.data.model.Combination
import com.lipo.menu.data.model.Dish
import kotlinx.coroutines.flow.Flow

interface CombinationRepository {
    fun getAllCombinations(): Flow<List<Combination>>
    fun searchCombinations(query: String): Flow<List<Combination>>
    fun getCombinationById(id: String): Flow<Combination?>
    suspend fun createCombination(
        name: String,
        description: String?,
        dishIds: List<String>
    ): Result<Combination>
    suspend fun updateCombination(
        id: String,
        name: String,
        description: String?
    ): Result<Combination>
    suspend fun deleteCombination(id: String): Result<Unit>
    suspend fun addDishToCombination(combinationId: String, dishId: String): Result<Unit>
    suspend fun removeDishFromCombination(combinationId: String, dishId: String): Result<Unit>
    suspend fun combinationNameExists(name: String, excludeId: String? = null): Boolean
    fun getCombinationCountForDish(dishId: String): Flow<Int>
}
