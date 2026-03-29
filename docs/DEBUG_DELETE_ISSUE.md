# 🗑️ 删除操作调试指南

## ❓ 问题描述

删除菜品时，只在本地删除了，没有从 Supabase 数据库中删除。

---

## 🔍 删除操作流程

```
用户点击删除按钮
    ↓
DishListViewModel.deleteDish(id)
    ↓
DeleteDishUseCase(id)
    ↓
DishRepository.deleteDish(id)
    ↓
1. 远程: remoteDataSource.deleteDish(id)
   - 调用 Supabase API
   - 设置 is_deleted = true
   - 更新 updated_at
    ↓
2. 本地: dishDao.softDeleteDish(id)
   - 设置 is_deleted = true
   - 更新 updated_at
```

---

## 🐛 调试步骤

### 步骤 1: 查看日志

```bash
# 清除旧日志
adb logcat -c

# 实时查看删除操作日志
adb logcat | grep -E "DishListViewModel|DishRepository|DishRemoteDataSource"
```

### 步骤 2: 执行删除操作

1. 打开应用
2. 长按菜品 → 选择删除
3. 确认删除
4. 观察日志输出

---

## 📊 预期的日志输出

### 成功的删除流程：

```
D/DishListViewModel: === User triggered delete dish ===
D/DishListViewModel: Dish ID: xxx-xxx-xxx
D/DishRepository: === Starting delete dish ===
D/DishRepository: Dish ID: xxx-xxx-xxx
D/DishRepository: Step 1: Syncing to cloud...
D/DishRemoteDataSource: === Starting to delete dish from Supabase ===
D/DishRemoteDataSource: Dish ID: xxx-xxx-xxx
D/DishRemoteDataSource: Update data: {"is_deleted":true,"updated_at":"2026-03-29T..."}
D/DishRemoteDataSource: === Dish deleted successfully in Supabase ===
D/DishRemoteDataSource: Dish ID: xxx-xxx-xxx
D/DishRemoteDataSource: is_deleted set to: true
D/DishRepository: ✓ Successfully synced to cloud
D/DishRepository: Step 2: Deleting from local database...
D/DishRepository: ✓ Successfully deleted from local database
D/DishRepository: === Delete dish completed successfully ===
D/DishListViewModel: ✓ Delete dish succeeded
```

### 可能的错误情况：

#### 情况 1: Supabase 权限问题
```
E/DishRemoteDataSource: === CRITICAL ERROR deleting dish ===
E/DishRemoteDataSource: Error type: HttpException
E/DishRemoteDataSource: Error message: 403 Forbidden
```

**原因：** Row Level Security (RLS) 策略阻止了更新操作

**解决方案：** 检查 Supabase RLS 策略

#### 情况 2: 网络连接失败
```
E/DishRemoteDataSource: === CRITICAL ERROR deleting dish ===
E/DishRemoteDataSource: Error type: UnknownHostException
E/DishRemoteDataSource: Error message: Unable to resolve host
```

**原因：** 设备无法连接到 Supabase 服务器

**解决方案：** 检查网络连接

#### 情况 3: 本地数据库失败
```
E/DishRepository: ✓ Successfully synced to cloud
E/DishRepository: Step 2: Deleting from local database...
E/DishRepository: ✗ Failed to delete from local database
```

**原因：** 本地数据库错误

**解决方案：** 检查 Room 数据库日志

---

## 🔧 检查 Supabase 配置

### 1. 检查 RLS 策略

在 **Supabase Dashboard** → **SQL Editor** 中运行：

```sql
-- 查看当前 RLS 策略
SELECT
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies
WHERE tablename = 'dishes';
```

### 2. 如果需要，禁用 RLS（开发环境）

```sql
-- 禁用 RLS（仅用于开发/测试）
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;

-- 或者添加允许所有操作的策略（开发环境）
CREATE POLICY "Allow all operations for development"
ON dishes
FOR ALL
USING (true)
WITH CHECK (true);
```

### 3. 为生产环境配置正确的 RLS

```sql
-- 启用 RLS
ALTER TABLE dishes ENABLE ROW LEVEL SECURITY;

-- 允许所有用户读取未删除的菜品
CREATE POLICY "Allow read active dishes"
ON dishes
FOR SELECT
USING (is_deleted = false OR is_deleted IS NULL);

-- 允许所有用户插入菜品
CREATE POLICY "Allow insert dishes"
ON dishes
FOR INSERT
WITH CHECK (true);

-- 允许所有用户更新菜品
CREATE POLICY "Allow update dishes"
ON dishes
FOR UPDATE
USING (true)
WITH CHECK (true);

-- 允许所有用户删除菜品（软删除）
CREATE POLICY "Allow delete dishes"
ON dishes
FOR UPDATE
USING (true)
WITH CHECK (true);
```

---

## 🧪 验证删除操作

### 方法 1: 在 Supabase Dashboard 中查看

```sql
-- 查看所有菜品（包括已删除的）
SELECT id, name, is_deleted, updated_at
FROM dishes
ORDER BY updated_at DESC
LIMIT 10;

-- 查看已删除的菜品
SELECT id, name, is_deleted, updated_at
FROM dishes
WHERE is_deleted = true
ORDER BY updated_at DESC;
```

### 方法 2: 使用 Supabase REST API

```bash
# 获取菜品详情
curl -X GET "https://YOUR_PROJECT.supabase.co/rest/v1/dishes?id=eq.DISH_ID" \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Authorization: Bearer YOUR_ANON_KEY"
```

---

## 🎯 常见问题和解决方案

### 问题 1: 日志显示成功，但 Supabase 中 is_deleted 仍为 false

**可能原因：**
1. RLS 策略阻止了更新
2. API 请求格式错误
3. Supabase 客户端配置问题

**解决方案：**
```sql
-- 检查是否有 RLS 阻止
SELECT * FROM pg_policies WHERE tablename = 'dishes';

-- 临时禁用 RLS 测试
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;

-- 再次尝试删除，如果成功，说明是 RLS 问题
```

### 问题 2: 日志显示 "403 Forbidden"

**原因：** Supabase 权限不足

**解决方案：**
1. 检查 API Key 是否正确
2. 检查 RLS 策略
3. 确保使用的是正确的 Supabase URL

### 问题 3: 本地删除成功，云端删除失败

**原因：** 远程数据源调用失败，但被捕获了

**解决方案：**
1. 查看详细日志中的错误信息
2. 检查网络连接
3. 验证 Supabase 配置

---

## 💡 快速修复脚本

如果你想让删除操作立即工作，可以运行以下 SQL：

```sql
-- ============================================
-- 快速修复：禁用 RLS（仅开发环境）
-- ============================================

-- 禁用 dishes 表的 RLS
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;

-- 禁用 today_menus 表的 RLS
ALTER TABLE today_menus DISABLE ROW LEVEL SECURITY;

-- 禁用 today_menu_dishes 表的 RLS
ALTER TABLE today_menu_dishes DISABLE ROW LEVEL SECURITY;

-- 验证 RLS 已禁用
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE tablename IN ('dishes', 'today_menus', 'today_menu_dishes');
```

**⚠️ 警告：** 这会禁用所有安全检查，仅用于开发环境！

---

## 📤 发送调试信息

如果问题仍然存在，请提供以下信息：

1. **完整的删除操作日志**
   ```bash
   adb logcat -d > delete_debug_log.txt
   ```

2. **Supabase RLS 策略查询结果**
   ```sql
   SELECT * FROM pg_policies WHERE tablename = 'dishes';
   ```

3. **菜品数据查询结果**
   ```sql
   SELECT id, name, is_deleted FROM dishes ORDER BY updated_at DESC LIMIT 5;
   ```

---

## 🎯 下一步

1. **等待最新 APK 构建完成**（包含删除调试日志）
2. **安装新 APK**
3. **执行删除操作**
4. **查看日志并发送给我**

日志会告诉我们确切的失败原因！🔍

---

**最后更新：** 2026-03-29
**包含调试日志的版本：** commit `585f535`
