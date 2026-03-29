package com.lipo.menu.data.remote

import com.lipo.menu.data.model.Dish
import com.lipo.menu.util.DateUtils
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import android.util.Log
import java.time.Instant
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
    private val TAG = "DishRemoteDataSource"

    /**
     * 同步菜品到云端
     */
    suspend fun upsertDish(dish: Dish): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing dish to Supabase: ${dish.name}")
            client.from("dishes").upsert(
                JsonObject(mapOf(
                    "id" to JsonPrimitive(dish.id),
                    "name" to JsonPrimitive(dish.name),
                    "description" to JsonPrimitive(dish.description ?: ""),
                    "created_at" to JsonPrimitive(DateUtils.toISO8601(dish.createdAt)),
                    "updated_at" to JsonPrimitive(DateUtils.toISO8601(dish.updatedAt)),
                    "is_deleted" to JsonPrimitive(dish.isDeleted),
                    "user_id" to JsonPrimitive("default-user")
                )),
                onConflict = "id"
            )
            Log.d(TAG, "Dish synced successfully: ${dish.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync dish: ${e.message}", e)
            throw RemoteDataSourceException("Failed to upsert dish: ${e.message}", e)
        }
    }

    /**
     * 从云端获取所有菜品
     */
    suspend fun fetchAllDishes(): List<Dish> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching dishes from Supabase")
            val result = client.from("dishes")
                .select {
                    filter {
                        eq("is_deleted", false)
                    }
                }
            Log.d(TAG, "Fetched dishes successfully. Result: ${result.toString().take(200)}")

            // 解析 JSON 数据
            val dishes = mutableListOf<Dish>()
            val resultString = result.toString()

            // supabase-kt 2.x 返回的数据需要手动解析
            // 使用正则表达式提取数据（简单方法）
            val pattern = """id=([^,}]+),.*?name=([^,}]+),.*?description=([^,}]*),.*?created_at=([^,}]+),.*?updated_at=([^,}]+)""".toRegex(RegexOption.DOT_MATCHES_ALL)

            // 更安全的方式：直接使用 kotlinx.serialization
            try {
                val jsonArray = kotlinx.serialization.json.Json.parseToJsonElement(resultString)
                if (jsonArray is kotlinx.serialization.json.JsonArray) {
                    jsonArray.forEach { element ->
                        try {
                            val obj = element.jsonObject
                            val dish = Dish(
                                id = obj["id"]?.jsonPrimitive?.content ?: "",
                                name = obj["name"]?.jsonPrimitive?.content ?: "",
                                description = obj["description"]?.jsonPrimitive?.contentOrNull,
                                createdAt = DateUtils.parseISO8601(obj["created_at"]?.jsonPrimitive?.content ?: Instant.now().toString()),
                                updatedAt = DateUtils.parseISO8601(obj["updated_at"]?.jsonPrimitive?.content ?: Instant.now().toString()),
                                isDeleted = obj["is_deleted"]?.jsonPrimitive?.booleanOrNull ?: false
                            )
                            if (dish.id.isNotEmpty() && dish.name.isNotEmpty()) {
                                dishes.add(dish)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse dish element: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse JSON array: ${e.message}")
            }

            Log.d(TAG, "Parsed ${dishes.size} dishes from Supabase")
            dishes
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch dishes: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 从云端删除菜品（软删除）
     */
    suspend fun deleteDish(dishId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting dish from Supabase: $dishId")
            client.from("dishes").update(
                JsonObject(mapOf(
                    "is_deleted" to JsonPrimitive(true),
                    "updated_at" to JsonPrimitive(DateUtils.toISO8601(DateUtils.getCurrentInstant()))
                ))
            ) {
                filter {
                    eq("id", dishId)
                }
            }
            Log.d(TAG, "Dish deleted successfully: $dishId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete dish: ${e.message}", e)
            throw RemoteDataSourceException("Failed to delete dish: ${e.message}", e)
        }
    }
}

/**
 * 远程数据源异常
 */
class RemoteDataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)
