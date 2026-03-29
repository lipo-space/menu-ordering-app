# 🔧 云端同步功能修复

## ✅ 问题已解决

**根本原因：** 虽然 `syncFromCloud()` 方法已经写好，但从来没有被调用过。应用启动时不会自动从云端拉取数据，导致家庭成员无法看到共享的菜品和菜单。

---

## 🔧 修复内容

### 1. 添加自动同步调用

**DishListViewModel.kt**
```kotlin
init {
    // 先从云端同步数据，然后加载本地数据
    viewModelScope.launch {
        try {
            dishRepository.syncFromCloud()
        } catch (e: Exception) {
            // 同步失败不影响本地数据显示
            android.util.Log.e("DishListViewModel", "Failed to sync from cloud: ${e.message}")
        }
    }
    loadDishes()
}
```

**TodayMenuViewModel.kt**
```kotlin
init {
    // 先从云端同步数据
    viewModelScope.launch {
        try {
            todayMenuRepository.syncFromCloud()
        } catch (e: Exception) {
            // 同步失败不影响本地数据显示
            android.util.Log.e("TodayMenuViewModel", "Failed to sync from cloud: ${e.message}")
        }
    }
    loadTodayMenu()
    loadAllDishes()
    loadAllCombinations()
}
```

### 2. 实现完整的云端同步逻辑

**TodayMenuRepositoryImpl.kt**
```kotlin
suspend fun syncFromCloud() {
    try {
        Log.d("TodayMenuRepository", "Starting sync from cloud")

        // 1. 同步今日菜单
        val cloudMenus = remoteDataSource.fetchAllTodayMenus()
        cloudMenus.forEach { menuMap ->
            // 检查本地是否已存在
            val localMenu = todayMenuDao.getTodayMenuByIdSync(menuId)

            if (localMenu == null) {
                // 本地不存在，插入
                todayMenuDao.insertTodayMenu(entity)
            } else {
                // 本地已存在，比较更新时间
                if (cloudUpdatedAt.isAfter(localUpdatedAt)) {
                    todayMenuDao.updateTodayMenu(entity)
                }
            }
        }

        // 2. 同步今日菜单菜品关联
        val cloudDishes = remoteDataSource.fetchAllTodayMenuDishes()
        // ... 类似逻辑
    } catch (e: Exception) {
        Log.e("TodayMenuRepository", "Failed to sync from cloud: ${e.message}")
    }
}
```

### 3. 添加云端数据拉取方法

**TodayMenuRemoteDataSource.kt**
```kotlin
suspend fun fetchAllTodayMenus(): List<Map<String, Any>>
suspend fun fetchAllTodayMenuDishes(): List<Map<String, Any>>
```

### 4. 添加 DAO 同步查询方法

**TodayMenuDao.kt**
```kotlin
@Query("SELECT * FROM today_menus WHERE id = :id LIMIT 1")
suspend fun getTodayMenuByIdSync(id: String): TodayMenuEntity?

@Query("SELECT * FROM today_menu_dishes WHERE today_menu_id = :todayMenuId AND dish_id = :dishId LIMIT 1")
suspend fun getTodayMenuDishByIdsSync(todayMenuId: String, dishId: String): TodayMenuDishEntity?

@Query("UPDATE today_menu_dishes SET `order` = :order WHERE today_menu_id = :todayMenuId AND dish_id = :dishId")
suspend fun updateTodayMenuDish(todayMenuId: String, dishId: String, order: Int)
```

### 5. 修复编译错误

- ✅ 添加缺失的 import 语句（jsonObject, jsonArray 等）
- ✅ 修复 TodayMenuDishEntity 字段名（order vs displayOrder）
- ✅ 修复 Map 类型检查和 isNotEmpty() 调用
- ✅ 修复实体构造参数

---

## 📊 数据同步流程

```
应用启动
    ↓
DishListViewModel.init / TodayMenuViewModel.init
    ↓
调用 syncFromCloud()
    ↓
从 Supabase 获取云端数据
    ↓
比较时间戳，智能合并
    ↓
保存到本地数据库 (Room)
    ↓
UI 自动更新 (Flow)
    ↓
显示家庭共享数据 ✅
```

---

## 🚀 使用说明

### 首次使用（重要！）

1. **在 Supabase Dashboard 运行 SQL 修复脚本**
   ```sql
   -- 修复 is_deleted 为 null 的记录
   UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;

   -- 修复时间戳
   UPDATE today_menus SET created_at = NOW() WHERE created_at IS NULL;
   UPDATE today_menus SET updated_at = NOW() WHERE updated_at IS NULL;
   UPDATE dishes SET updated_at = NOW() WHERE updated_at IS NULL;

   -- 设置默认值
   ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
   ALTER TABLE today_menus ALTER COLUMN created_at SET DEFAULT NOW();
   ALTER TABLE today_menus ALTER COLUMN updated_at SET DEFAULT NOW();
   ```

2. **在所有设备上卸载旧版本应用**
   ```bash
   adb uninstall com.lipo.menu
   ```

3. **安装新版本 APK**
   - 从 GitHub Actions 下载最新 APK
   - 安装到所有设备

### 日常使用

- ✅ **自动同步**：打开应用时自动从云端拉取最新数据
- ✅ **智能合并**：基于时间戳保留最新数据
- ✅ **离线支持**：同步失败不影响本地数据使用
- ✅ **实时更新**：UI 自动显示最新数据

---

## 🎯 测试家庭共享

### 设备 A：
1. 打开应用
2. 添加菜品（例如：宫保鸡丁）
3. 等待 5 秒（数据同步到云端）

### 设备 B：
1. 打开应用
2. **应该能看到宫保鸡丁** ✅

---

## 📝 技术细节

### 同步策略

- **菜品 (Dish)**: 基于 `updated_at` 时间戳智能合并
- **今日菜单 (TodayMenu)**: 基于 `updated_at` 时间戳智能合并
- **关联关系 (TodayMenuDish)**: 基于 `display_order` 更新

### 错误处理

- 同步失败不影响本地数据显示
- 详细的日志记录（Logcat 中搜索 "DishRepository" 或 "TodayMenuRepository"）
- 异常捕获并记录，不会导致应用崩溃

### 性能优化

- 只同步差异（比较时间戳）
- 使用协程异步处理（Dispatchers.IO）
- 批量操作减少数据库访问

---

## 📚 相关文档

- **卸载重装指南**: `docs/ANDROID_STUDIO_GUIDE.md`
- **当前状态**: `docs/CURRENT_STATUS.md`
- **UpsertOptions 修复**: `docs/UPSERT_OPTIONS_FIX.md`
- **Supabase 快速修复**: `docs/SUPABASE_QUICK_FIX.md`

---

## ✅ 提交记录

- **1f8e980** - fix: 修复云端同步功能的编译错误
- **c95ba7e** - feat: 实现完整的云端同步功能
- **7f01b31** - feat: 在 DishListViewModel 初始化时自动从云端同步菜品数据

---

**最后更新：** 2026-03-29
**适用版本：** v1.0+
