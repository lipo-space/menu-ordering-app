package com.lipo.menu.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lipo.menu.data.local.database.entities.CombinationDishEntity
import com.lipo.menu.data.local.database.entities.CombinationEntity
import com.lipo.menu.data.local.database.entities.CombinationWithDishes
import kotlinx.coroutines.flow.Flow

@Dao
interface CombinationDao {
    @Transaction
    @Query("SELECT * FROM combinations ORDER BY updated_at DESC")
    fun getAllCombinationsWithDishes(): Flow<List<CombinationWithDishes>>

    @Query("SELECT * FROM combinations ORDER BY name ASC")
    fun getAllCombinations(): Flow<List<CombinationEntity>>

    @Transaction
    @Query("SELECT * FROM combinations WHERE id = :id")
    fun getCombinationWithDishesById(id: String): Flow<CombinationWithDishes?>

    @Query("SELECT * FROM combinations WHERE id = :id")
    fun getCombinationById(id: String): Flow<CombinationEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCombination(combination: CombinationEntity)

    @Query("DELETE FROM combinations WHERE id = :id")
    suspend fun deleteCombination(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombinationDish(combinationDish: CombinationDishEntity)

    @Query("DELETE FROM combination_dishes WHERE combination_id = :combinationId AND dish_id = :dishId")
    suspend fun deleteCombinationDish(combinationId: String, dishId: String)

    @Query("SELECT * FROM combination_dishes WHERE combination_id = :combinationId ORDER BY `order` ASC")
    fun getCombinationDishes(combinationId: String): Flow<List<CombinationDishEntity>>

    @Query("SELECT COUNT(*) > 0 FROM combinations WHERE LOWER(name) = LOWER(:name) AND (:excludeId IS NULL OR id != :excludeId)")
    suspend fun combinationNameExists(name: String, excludeId: String? = null): Boolean

    @Query("UPDATE combinations SET name = :name, description = :description, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateCombination(id: String, name: String, description: String?, updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM combinations WHERE LOWER(name) LIKE LOWER(:query) ORDER BY updated_at DESC")
    fun searchCombinationsWithDishesByName(query: String): Flow<List<CombinationWithDishes>>

    @Query("SELECT * FROM combinations WHERE LOWER(name) LIKE LOWER(:query) ORDER BY updated_at DESC")
    fun searchCombinationsByName(query: String): Flow<List<CombinationEntity>>

    @Query("SELECT COUNT(*) FROM combination_dishes WHERE dish_id = :dishId")
    fun getCombinationCountForDish(dishId: String): Flow<Int>
}
