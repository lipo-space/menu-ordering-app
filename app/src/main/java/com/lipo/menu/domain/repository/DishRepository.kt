package com.lipo.menu.domain.repository

import com.lipo.menu.data.model.Dish
import kotlinx.coroutines.flow.Flow

interface DishRepository {
    fun getAllDishes(): Flow<List<Dish>>
    fun searchDishes(query: String): Flow<List<Dish>>
    fun getDishById(id: String): Flow<Dish?>
    suspend fun addDish(name: String, description: String?): Result<Dish>
    suspend fun updateDish(id: String, name: String, description: String?): Result<Dish>
    suspend fun deleteDish(id: String): Result<Unit>
    suspend fun dishNameExists(name: String, excludeId: String? = null): Boolean
}
