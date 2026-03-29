package com.lipo.menu.data.remote

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.UpsertOptions
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
            Log.d(TAG, "Syncing today menu to Supabase: ${todayMenu.id}, date: ${todayMenu.date}")
            // 使用 date 作为冲突解决列（因为 date 有唯一约束）
            client.from("today_menus").upsert(
                JsonObject(mapOf(
                    "id" to JsonPrimitive(todayMenu.id),
                    "date" to JsonPrimitive(DateUtils.formatForStorage(todayMenu.date)),
                    "created_at" to JsonPrimitive(DateUtils.toISO8601(todayMenu.createdAt)),
                    "updated_at" to JsonPrimitive(DateUtils.toISO8601(todayMenu.updatedAt)),
                    "user_id" to JsonPrimitive("default-user")
                )),
                upsertOptions = UpsertOptions(
                    onConflict = "date"
                )
            )
            Log.d(TAG, "Today menu synced successfully: ${todayMenu.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync today menu: ${e.message}", e)
            throw RemoteDataSourceException("Failed to upsert today menu: ${e.message}", e)
        }
    }

    /**
     * 同步今日菜单菜品关联到云端
     */
    suspend fun upsertTodayMenuDish(todayMenuId: String, dishId: String, displayOrder: Int): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing today menu dish to Supabase: $todayMenuId-$dishId")
            // 使用复合主键作为冲突解决
            client.from("today_menu_dishes").upsert(
                JsonObject(mapOf(
                    "today_menu_id" to JsonPrimitive(todayMenuId),
                    "dish_id" to JsonPrimitive(dishId),
                    "display_order" to JsonPrimitive(displayOrder)
                )),
                upsertOptions = UpsertOptions(
                    onConflict = "today_menu_id,dish_id"
                )
            )
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
     * 从云端删除今日菜单的所有菜品关联
     */
    suspend fun deleteTodayMenuDishesByMenu(todayMenuId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting all dishes for today menu from Supabase: $todayMenuId")
            client.from("today_menu_dishes").delete() {
                filter {
                    eq("today_menu_id", todayMenuId)
                }
            }
            Log.d(TAG, "Today menu dishes deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete today menu dishes: ${e.message}", e)
            throw RemoteDataSourceException("Failed to delete today menu dishes: ${e.message}", e)
        }
    }
}
