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
            Log.d(TAG, "=== Starting to fetch dishes from Supabase ===")
            Log.d(TAG, "Client initialized: ${client != null}")

            val result = client.from("dishes")
                .select {
                    filter {
                        eq("is_deleted", false)
                    }
                }

            val resultString = result.toString()
            Log.d(TAG, "=== Raw result from Supabase ===")
            Log.d(TAG, "Result length: ${resultString.length}")
            Log.d(TAG, "Result preview: ${resultString.take(500)}")

            // 解析 JSON 数据
            val dishes = mutableListOf<Dish>()

            try {
                val jsonArray = kotlinx.serialization.json.Json.parseToJsonElement(resultString)
                Log.d(TAG, "Parsed JSON type: ${jsonArray::class.simpleName}")

                if (jsonArray is kotlinx.serialization.json.JsonArray) {
                    Log.d(TAG, "Array size: ${jsonArray.size}")

                    jsonArray.forEachIndexed { index, element ->
                        try {
                            Log.d(TAG, "Processing element $index")
                            val obj = element.jsonObject

                            val id = obj["id"]?.jsonPrimitive?.content ?: ""
                            val name = obj["name"]?.jsonPrimitive?.content ?: ""
                            val description = obj["description"]?.jsonPrimitive?.contentOrNull
                            val createdAtStr = obj["created_at"]?.jsonPrimitive?.content ?: ""
                            val updatedAtStr = obj["updated_at"]?.jsonPrimitive?.content ?: ""
                            val isDeleted = obj["is_deleted"]?.jsonPrimitive?.booleanOrNull ?: false

                            Log.d(TAG, "  Dish $index: id=$id, name=$name, description=$description")

                            if (id.isNotEmpty() && name.isNotEmpty()) {
                                val dish = Dish(
                                    id = id,
                                    name = name,
                                    description = description,
                                    createdAt = if (createdAtStr.isNotEmpty()) {
                                        DateUtils.parseISO8601(createdAtStr)
                                    } else {
                                        Instant.now()
                                    },
                                    updatedAt = if (updatedAtStr.isNotEmpty()) {
                                        DateUtils.parseISO8601(updatedAtStr)
                                    } else {
                                        Instant.now()
                                    },
                                    isDeleted = isDeleted
                                )
                                dishes.add(dish)
                                Log.d(TAG, "  Successfully added dish: $name")
                            } else {
                                Log.w(TAG, "  Skipped dish with empty id or name")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse dish element $index: ${e.message}", e)
                        }
                    }
                } else {
                    Log.e(TAG, "Result is not a JSON array! Type: ${jsonArray::class.simpleName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse JSON: ${e.message}", e)
                Log.e(TAG, "Result string that failed to parse: ${resultString.take(1000)}")
            }

            Log.d(TAG, "=== Finished parsing. Total dishes: ${dishes.size} ===")
            dishes
        } catch (e: Exception) {
            Log.e(TAG, "=== CRITICAL ERROR fetching dishes ===", e)
            Log.e(TAG, "Error type: ${e::class.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 从云端删除菜品（软删除）
     */
    suspend fun deleteDish(dishId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Starting to delete dish from Supabase ===")
            Log.d(TAG, "Dish ID: $dishId")

            val updateData = JsonObject(mapOf(
                "is_deleted" to JsonPrimitive(true),
                "updated_at" to JsonPrimitive(DateUtils.toISO8601(DateUtils.getCurrentInstant()))
            ))
            Log.d(TAG, "Update data: $updateData")

            client.from("dishes").update(updateData) {
                filter {
                    eq("id", dishId)
                }
            }

            Log.d(TAG, "=== Dish deleted successfully in Supabase ===")
            Log.d(TAG, "Dish ID: $dishId")
            Log.d(TAG, "is_deleted set to: true")
        } catch (e: Exception) {
            Log.e(TAG, "=== CRITICAL ERROR deleting dish ===", e)
            Log.e(TAG, "Dish ID: $dishId")
            Log.e(TAG, "Error type: ${e::class.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            throw RemoteDataSourceException("Failed to delete dish: ${e.message}", e)
        }
    }
}

/**
 * 远程数据源异常
 */
class RemoteDataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)
