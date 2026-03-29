# 🔍 数据同步调试指南

## ❓ 问题描述

打开菜品页面时，看不到 Supabase 数据库中已有的菜品信息。

---

## 🐛 调试步骤

### 步骤 1: 查看日志输出

我已经添加了详细的调试日志。安装最新 APK 后，使用以下命令查看日志：

```bash
# 清除旧日志
adb logcat -c

# 实时查看同步日志
adb logcat | grep -E "DishListViewModel|DishRepository|DishRemoteDataSource"
```

### 步骤 2: 重现问题

1. 打开应用
2. 导航到"菜品"页面
3. 观察日志输出

---

## 📊 预期的日志输出

### 成功的同步流程：

```
D/DishListViewModel: === ViewModel init started ===
D/DishListViewModel: Calling syncFromCloud()...
D/DishRepository: === Starting sync from cloud ===
D/DishRepository: RemoteDataSource: com.lipo.menu.data.remote.DishRemoteDataSource@xxxxx
D/DishRemoteDataSource: === Starting to fetch dishes from Supabase ===
D/DishRemoteDataSource: Client initialized: true
D/DishRemoteDataSource: === Raw result from Supabase ===
D/DishRemoteDataSource: Result length: 234
D/DishRemoteDataSource: Result preview: [{"id":"uuid","name":"宫保鸡丁","description":"...","is_deleted":false,...}]
D/DishRemoteDataSource: Parsed JSON type: JsonArray
D/DishRemoteDataSource: Array size: 2
D/DishRemoteDataSource: Processing element 0
D/DishRemoteDataSource:   Dish 0: id=xxx, name=宫保鸡丁, description=...
D/DishRemoteDataSource:   Successfully added dish: 宫保鸡丁
D/DishRemoteDataSource: Processing element 1
D/DishRemoteDataSource:   Dish 1: id=xxx, name=红烧肉, description=...
D/DishRemoteDataSource:   Successfully added dish: 红烧肉
D/DishRemoteDataSource: === Finished parsing. Total dishes: 2 ===
D/DishRepository: Fetched 2 dishes from cloud
D/DishRepository: Processing dish 1/2: 宫保鸡丁
D/DishRepository: ✓ Inserted dish from cloud: 宫保鸡丁
D/DishRepository: Processing dish 2/2: 红烧肉
D/DishRepository: ✓ Inserted dish from cloud: 红烧肉
D/DishRepository: === Sync completed. Synced 2 dishes ===
D/DishListViewModel: syncFromCloud() completed successfully
```

### 可能的错误情况：

#### 情况 1: Supabase 连接失败
```
E/DishRemoteDataSource: === CRITICAL ERROR fetching dishes ===
E/DishRemoteDataSource: Error type: HttpException
E/DishRemoteDataSource: Error message: HTTP request failed
```

#### 情况 2: 数据解析失败
```
E/DishRemoteDataSource: Failed to parse JSON: ...
E/DishRemoteDataSource: Result string that failed to parse: ...
```

#### 情况 3: 数据库插入失败
```
E/DishRepository: ✗ Failed to sync dish xxx: UNIQUE constraint failed
```

---

## 🔧 常见问题和解决方案

### 问题 1: 看不到任何日志

**原因：** Logcat 过滤器设置错误或应用未安装

**解决方案：**
```bash
# 确认应用已安装
adb shell pm list packages | grep com.lipo.menu

# 清除日志并重新运行
adb logcat -c
adb logcat *:D | grep -E "Dish|TodayMenu"
```

### 问题 2: "Client initialized: false"

**原因：** Supabase 客户端初始化失败

**解决方案：**
1. 检查 `SupabaseConfig` 是否正确配置
2. 检查网络连接
3. 检查 Supabase URL 和 API Key 是否正确

### 问题 3: "Result length: 0"

**原因：** 数据库中没有数据，或查询条件错误

**解决方案：**
1. 在 Supabase Dashboard 中验证数据：
   ```sql
   SELECT * FROM dishes WHERE is_deleted = false;
   ```
2. 检查 `user_id` 过滤（如果有）
3. 运行 SQL 修复脚本（见下方）

### 问题 4: JSON 解析失败

**原因：** Supabase 返回的数据格式与预期不符

**解决方案：**
查看日志中的 "Result preview"，检查数据格式

---

## 💾 Supabase 数据库检查

### 在 Supabase Dashboard 中运行：

```sql
-- 1. 检查是否有菜品数据
SELECT COUNT(*) as total_dishes FROM dishes;
SELECT COUNT(*) as active_dishes FROM dishes WHERE is_deleted = false;
SELECT COUNT(*) as deleted_dishes FROM dishes WHERE is_deleted = true;

-- 2. 查看所有菜品
SELECT id, name, description, is_deleted, created_at, updated_at
FROM dishes
ORDER BY created_at DESC;

-- 3. 检查 is_deleted 是否为 null
SELECT COUNT(*) as null_is_deleted FROM dishes WHERE is_deleted IS NULL;

-- 4. 如果 is_deleted 有 null 值，运行修复
UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
```

---

## 🧪 手动测试同步

如果需要手动测试同步功能，可以添加一个测试按钮：

### 在菜品列表页面添加测试按钮：

```kotlin
// 在 DishListScreen.kt 中添加
Button(
    onClick = {
        viewModelScope.launch {
            try {
                dishRepository.syncFromCloud()
            } catch (e: Exception) {
                Log.e("ManualSync", "Failed: ${e.message}", e)
            }
        }
    }
) {
    Text("手动同步")
}
```

---

## 📤 发送调试信息

如果问题仍然存在，请提供以下信息：

1. **完整的日志输出**
   ```bash
   adb logcat -d > debug_log.txt
   ```

2. **Supabase 数据查询结果**
   ```sql
   SELECT * FROM dishes WHERE is_deleted = false LIMIT 5;
   ```

3. **应用版本信息**
   - APK 构建时间
   - 最新 commit hash

4. **设备信息**
   - Android 版本
   - 设备型号

---

## 🎯 下一步

1. 安装最新的 APK（包含调试日志）
2. 运行 `adb logcat -c` 清除日志
3. 打开应用到菜品页面
4. 复制完整的日志输出
5. 发送日志，我会帮你分析问题

---

**最后更新：** 2026-03-29
**包含调试日志的版本：** commit `fa2262f`
