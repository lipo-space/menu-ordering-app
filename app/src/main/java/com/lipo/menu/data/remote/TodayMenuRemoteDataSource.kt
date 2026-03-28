package com.lipo.menu.data.remote

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.data.model.TodayMenuDish
import com.lipo.menu.util.DateUtils
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 今日菜单远程数据源
 * 负责与 Supabase today_menus 和 today_menu_dishes 表交互
 */
@Singleton
class TodayMenuRemoteDataSource @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) {

    private val client get() = supabaseConfig.getClient()
    private val TAG = "TodayMenuRemoteDataSource"

    /**
     * 同步今日菜单到云端
     */
    suspend fun upsertTodayMenu(todayMenu: TodayMenu): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing today menu to Supabase: ${todayMenu.id}")
            // 插入菜单
            client.from("today_menus").upsert(
                JsonObject(mapOf(
                    "id" to JsonPrimitive(todayMenu.id),
                    "date" to JsonPrimitive(DateUtils.formatForStorage(todayMenu.date)),
                    "created_at" to JsonPrimitive(DateUtils.toISO8601(todayMenu.createdAt)),
                    "updated_at" to JsonPrimitive(DateUtils.toISO8601(todayMenu.updatedAt)),
                    "user_id" to JsonPrimitive("default-user")
                ))
            ) {
                filter {
                    eq("id", todayMenu.id)
                }
            }
            Log.d(TAG, "Today menu synced successfully: ${todayMenu.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync today menu: ${e.message}", e)
            throw RemoteDataSourceException("Failed to upsert today menu: ${e.message}", e)
        }
    }

    /**
     * 同步今日菜单菜品关联到云端
     */
    suspend fun upsertTodayMenuDish(todayMenuDish: TodayMenuDish): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing today menu dish to Supabase: ${todayMenuDish.todayMenuId}-${todayMenuDish.dishId}")
            client.from("today_menu_dishes").upsert(
                JsonObject(mapOf(
                    "today_menu_id" to JsonPrimitive(todayMenuDish.todayMenuId),
                    "dish_id" to JsonPrimitive(todayMenuDish.dishId),
                    "display_order" to JsonPrimitive(todayMenuDish.displayOrder)
                ))
            ) {
                filter {
                    and {
                        eq("today_menu_id", todayMenuDish.todayMenuId)
                        eq("dish_id", todayMenuDish.dishId)
                    }
                }
            }
            Log.d(TAG, "Today menu dish synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync today menu dish: ${e.message}", e)
            throw RemoteDataSourceException("Failed to upsert today menu dish: ${e.message}", e)
        }
    }

    /**
     * 从云端删除今日菜单菜品关联
     */
    suspend fun deleteTodayMenuDish(todayMenuId: String, dishId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting today menu dish from Supabase: $todayMenuId-$dishId")
            client.from("today_menu_dishes").delete() {
                filter {
                    and {
                        eq("today_menu_id", todayMenuId)
                        eq("dish_id", dishId)
                    }
                }
            }
            Log.d(TAG, "Today menu dish deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete today menu dish: ${e.message}", e)
            throw RemoteDataSourceException("Failed to delete today menu dish: ${e.message}", e)
        }
    }

    /**
     * 从云端删除今日菜单
     */
    suspend fun deleteTodayMenu(todayMenuId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting today menu from Supabase: $todayMenuId")
            client.from("today_menus").delete() {
                filter {
                    eq("id", todayMenuId)
                }
            }
            Log.d(TAG, "Today menu deleted successfully: $todayMenuId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete today menu: ${e.message}", e)
            throw RemoteDataSourceException("Failed to delete today menu: ${e.message}", e)
        }
    }

    /**
     * 从云端获取指定日期的今日菜单
     */
    suspend fun fetchTodayMenuByDate(date: java.time.LocalDate): TodayMenu? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching today menu from Supabase for date: $date")
            val dateString = DateUtils.formatForStorage(date)
            val result = client.from("today_menus")
                .select {
                    filter {
                        eq("date", dateString)
                    }
                    single()
                }

            if (result != null) {
                val json = result.jsonObject
                TodayMenu(
                    id = json["id"]?.jsonPrimitive?.content ?: "",
                    date = DateUtils.toLocalDate(json["date"]?.jsonPrimitive?.content?.toLong() ?: 0),
                    createdAt = DateUtils.parseISO8601(json["created_at"]?.jsonPrimitive?.content ?: ""),
                    updatedAt = DateUtils.parseISO8601(json["updated_at"]?.jsonPrimitive?.content ?: "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch today menu: ${e.message}", e)
            null
        }
    }
}
