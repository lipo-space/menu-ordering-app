# 🐛 调试数据同步问题

## 问题现象
- ✅ 菜品可以成功存入数据库
- ❌ 其他用户看不到共享的菜品

## 调试步骤

### 第一步：检查 Supabase 中的数据

在 **Supabase Dashboard** → **SQL Editor** 中运行：

```sql
-- 查看所有菜品（包括已删除的）
SELECT
    id,
    name,
    description,
    created_at,
    updated_at,
    is_deleted,
    user_id
FROM dishes
ORDER BY created_at DESC;
```

**检查以下几点：**
1. 是否有数据？
2. `is_deleted` 的值是什么？（应该是 `false`，而不是 `null`）
3. `user_id` 的值是什么？（应该是 `'default-user'`）

### 第二步：检查应用日志

在设备上运行：

```bash
# 清除旧日志
adb logcat -c

# 启动应用并查看同步日志
adb logcat | grep -E "DishRepository|DishRemoteDataSource"
```

**关键日志：**

成功的日志：
```
D/DishRemoteDataSource: Fetching dishes from Supabase
D/DishRemoteDataSource: Fetched dishes successfully
D/DishRemoteDataSource: Parsed 5 dishes from Supabase
D/DishRepository: Starting sync from cloud
D/DishRepository: Inserted dish from cloud: 宫保鸡丁
D/DishRepository: Sync completed. Synced 5 dishes
```

失败的日志：
```
E/DishRemoteDataSource: Failed to fetch dishes: ...
E/DishRepository: Failed to sync from cloud: ...
```

### 第三步：手动测试数据解析

在 **Supabase Dashboard** → **SQL Editor** 中测试查询：

```sql
-- 测试查询条件
SELECT COUNT(*)
FROM dishes
WHERE is_deleted = false;
```

如果返回 0，说明：
1. `is_deleted` 可能是 `null` 而不是 `false`
2. 数据可能没有插入成功

### 第四步：修复 is_deleted 为 null 的问题

如果 `is_deleted` 是 `null`，运行：

```sql
-- 更新所有 null 为 false
UPDATE dishes
SET is_deleted = false
WHERE is_deleted IS NULL;

-- 修改默认值
ALTER TABLE dishes
ALTER COLUMN is_deleted SET DEFAULT false;
```

### 第五步：检查数据是否真的插入了

```sql
-- 查看最近的菜品
SELECT
    id,
    name,
    created_at,
    is_deleted
FROM dishes
WHERE is_deleted = false
ORDER BY created_at DESC
LIMIT 10;
```

## 常见问题和解决方案

### 问题 1：is_deleted 字段为 null

**症状：**
- SQL 查询 `WHERE is_deleted = false` 返回 0 条记录
- 但 `SELECT * FROM dishes` 能看到数据

**原因：**
Supabase 的 `is_deleted` 字段可能是 `null` 而不是 `false`

**解决方案：**
```sql
UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
```

### 问题 2：数据解析失败

**症状：**
- 日志显示 "Failed to parse JSON array"
- 或者 "Parsed 0 dishes from Supabase"

**原因：**
1. Supabase 返回的数据格式不是预期的 JSON
2. 字段名不匹配

**调试：**
在 `DishRemoteDataSource.kt` 的 `fetchAllDishes()` 方法中添加日志：

```kotlin
Log.d(TAG, "Result string: $resultString")
```

然后重新运行，查看实际返回的数据格式。

### 问题 3：同步时机太早

**症状：**
- 应用启动时同步
- 但 UI 显示时同步还没完成

**解决方案：**
在 UI 中添加加载状态，或者确保同步在数据加载前完成。

### 问题 4：网络或权限问题

**症状：**
- 日志显示 "Permission denied"

**检查：**
```bash
adb shell dumpsys package com.lipo.menu | grep permission
```

确认应用有 INTERNET 权限。

## 快速诊断脚本

将以下内容保存为 `debug_sync.sh`：

```bash
#!/bin/bash
echo "=== 检查应用日志 ==="
adb logcat -d | grep -E "DishRepository|DishRemoteDataSource" | tail -50

echo ""
echo "=== 检查网络权限 ==="
adb shell dumpsys package com.lipo.menu | grep -A 5 "permission"

echo ""
echo "=== 检查应用版本 ==="
adb shell dumpsys package com.lipo.menu | grep versionName
```

运行：
```bash
chmod +x debug_sync.sh
./debug_sync.sh
```

## 临时解决方案：手动触发同步

如果自动同步不工作
，可以手动触发：

1. 在应用中添加一个"同步"按钮
2. 或者在设置中添加"从云端同步"选项
3. 调用 `dishRepository.syncFromCloud()`

## 验证数据已同步

在设备 B 上：

1. 打开应用
2. 查看 Logcat 日志
3. 应该看到：
   ```
   D/DishRepository: Starting sync from cloud
   D/DishRepository: Inserted dish from cloud: XXX
   D/DishRepository: Sync completed. Synced N dishes
   ```
4. 在应用中查看菜品列表
5. 应该能看到设备 A 添加的菜品
