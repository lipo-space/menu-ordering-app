package com.lipo.menu.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lipo.menu.data.local.database.entities.DishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes WHERE is_deleted = 0 ORDER BY name ASC")
    fun getAllDishes(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' AND is_deleted = 0 ORDER BY name ASC LIMIT 100")
    fun searchDishes(query: String): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getDishById(id: String): Flow<DishEntity?>

    @Query("SELECT * FROM dishes WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getDishByIdSync(id: String): DishEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDish(dish: DishEntity)

    @Update
    suspend fun updateDish(dish: DishEntity)

    @Query("UPDATE dishes SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteDish(id: String, updatedAt: Long)

    @Query("SELECT COUNT(*) > 0 FROM dishes WHERE LOWER(name) = LOWER(:name) AND is_deleted = 0 AND (:excludeId IS NULL OR id != :excludeId)")
    suspend fun dishNameExists(name: String, excludeId: String? = null): Boolean
}
