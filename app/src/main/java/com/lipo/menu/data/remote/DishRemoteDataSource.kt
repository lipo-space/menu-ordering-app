package com.lipo.menu.data.remote

import com.lipo.menu.data.model.Dish
import com.lipo.menu.util.DateUtils
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 菜品远程数据源
 * 负责与 Supabase dishes 表交互
 */
@Singleton
class DishRemoteDataSource @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) {

    private val client get() = supabaseConfig.getClient()
    private val postgrest get() = client.postgrest

    /**
     * 同步菜品到云端
     */
    suspend fun upsertDish(dish: Dish) = withContext(Dispatchers.IO) {
        try {
            postgrest["dishes"].upsert(mapOf(
                "id" to dish.id,
                "name" to dish.name,
                "description" to dish.description,
                "created_at" to DateUtils.toISO8601(dish.createdAt),
                "updated_at" to DateUtils.toISO8601(dish.updatedAt),
                "is_deleted" to dish.isDeleted,
                "user_id" to "default-user"  // 暂时使用默认用户
            ))
        } catch (e: Exception) {
            throw RemoteDataSourceException("Failed to upsert dish: ${e.message}", e)
        }
    }

    /**
     * 从云端获取所有菜品
     */
    suspend fun fetchAllDishes(): List<Dish> = withContext(Dispatchers.IO) {
        try {
            val result = postgrest["dishes"]
                .select {
                    filter {
                        eq("is_deleted", false)
                    }
                }

            result.data.map { json ->
                Dish(
                    id = json["id"].toString(),
                    name = json["name"].toString(),
                    description = json["description"]?.toString(),
                    createdAt = DateUtils.parseISO8601(json["created_at"].toString()),
                    updatedAt = DateUtils.parseISO8601(json["updated_at"].toString()),
                    isDeleted = json["is_deleted"]?.toString()?.toBoolean() ?: false
                )
            }
        } catch (e: Exception) {
            throw RemoteDataSourceException("Failed to fetch dishes: ${e.message}", e)
        }
    }

    /**
     * 从云端删除菜品（软删除）
     */
    suspend fun deleteDish(dishId: String) = withContext(Dispatchers.IO) {
        try {
            postgrest["dishes"].update({
                set("is_deleted", true)
                set("updated_at", DateUtils.toISO8601(DateUtils.getCurrentInstant()))
            }) {
                filter {
                    eq("id", dishId)
                }
            }
        } catch (e: Exception) {
            throw RemoteDataSourceException("Failed to delete dish: ${e.message}", e)
        }
    }
}

/**
 * 远程数据源异常
 */
class RemoteDataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)
