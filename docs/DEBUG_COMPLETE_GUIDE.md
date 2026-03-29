# 🔧 完整调试指南

## 📊 当前状态

### ✅ 已修复的编译错误
- isNull 引用错误
- UpsertOptions 引用错误
- jsonObject import 缺失
- DAO 方法缺失
- 实体字段名错误
- Map 类型检查错误
- updateTodayMenu 参数错误
- 单元测试参数缺失

### ⚠️ 待解决的问题
1. **数据同步问题**: 打开菜品页面看不到 Supabase 中的数据
2. **删除同步问题**: 删除菜品只在本地删除，不同步到云端

---

## 🐛 问题 1: 数据同步失败

### 症状
- 设备 A 添加菜品成功
- Supabase Dashboard 能看到数据
- 设备 B 打开应用看不到数据

### 调试步骤

```bash
# 1. 清除日志
adb logcat -c

# 2. 查看同步日志
adb logcat | grep -E "DishListViewModel|DishRepository|DishRemoteDataSource"
```

### 可能的原因

#### 原因 1: Supabase 中 is_deleted 为 NULL

**检查：**
```sql
SELECT COUNT(*) FROM dishes WHERE is_deleted IS NULL;
```

**修复：**
```sql
UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
```

#### 原因 2: RLS 策略阻止查询

**检查：**
```sql
SELECT * FROM pg_policies WHERE tablename = 'dishes';
```

**临时修复（开发环境）：**
```sql
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;
```

#### 原因 3: 网络连接问题

**检查日志：**
```
E/DishRemoteDataSource: === CRITICAL ERROR fetching dishes ===
E/DishRemoteDataSource: Error type: UnknownHostException
```

**解决：** 检查网络连接和 Supabase URL 配置

---

## 🗑️ 问题 2: 删除同步失败

### 症状
- 用户删除菜品
- 本地菜品消失
- Supabase 中 is_deleted 仍为 false

### 调试步骤

```bash
# 查看删除操作日志
adb logcat | grep -E "deleteDish|DeleteDish"
```

### 可能的原因

#### 原因 1: RLS 策略阻止更新

**检查日志：**
```
E/DishRemoteDataSource: Error type: HttpException
E/DishRemoteDataSource: Error message: 403 Forbidden
```

**修复：**
```sql
-- 方法 1: 禁用 RLS（开发环境）
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;

-- 方法 2: 添加更新策略（推荐）
CREATE POLICY "Allow update dishes"
ON dishes
FOR UPDATE
USING (true)
WITH CHECK (true);
```

#### 原因 2: Supabase 客户端配置错误

**检查：** 查看 `SupabaseConfig` 中的 URL 和 API Key

#### 原因 3: API 调用格式错误

**检查日志：**
```
D/DishRemoteDataSource: Update data: {"is_deleted":true,...}
```

---

## 🚀 快速修复脚本

### 完整修复脚本（在 Supabase Dashboard SQL Editor 中运行）

```sql
-- ============================================
-- 1. 修复数据
-- ============================================

-- 修复 is_deleted 为 null 的记录
UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;
UPDATE today_menus SET created_at = NOW() WHERE created_at IS NULL;
UPDATE today_menus SET updated_at = NOW() WHERE updated_at IS NULL;
UPDATE dishes SET updated_at = NOW() WHERE updated_at IS NULL;

-- ============================================
-- 2. 设置默认值
-- ============================================

ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
ALTER TABLE today_menus ALTER COLUMN created_at SET DEFAULT NOW();
ALTER TABLE today_menus ALTER COLUMN updated_at SET DEFAULT NOW();

-- ============================================
-- 3. 禁用 RLS（开发环境 - 快速测试）
-- ============================================

ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menus DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menu_dishes DISABLE ROW LEVEL SECURITY;

-- ============================================
-- 4. 验证修复
-- ============================================

-- 检查 null 值
SELECT
    'dishes' as table_name,
    COUNT(*) as total,
    COUNT(CASE WHEN is_deleted IS NULL THEN 1 END) as null_count
FROM dishes;

-- 检查 RLS 状态
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE tablename IN ('dishes', 'today_menus', 'today_menu_dishes');
```

---

## 📱 完整测试流程

### 步骤 1: 准备环境

```bash
# 1. 等待 GitHub Actions 构建完成
# 2. 下载最新 APK
# 3. 卸载旧版本
adb uninstall com.lipo.menu

# 4. 安装新 APK
adb install app-debug.apk
```

### 步骤 2: 运行 Supabase 修复脚本

在 **Supabase Dashboard** → **SQL Editor** 中运行上面的完整修复脚本

### 步骤 3: 测试数据同步

```bash
# 清除日志
adb logcat -c

# 打开应用到菜品页面，观察日志
adb logcat | grep -E "DishRepository|DishRemoteDataSource"
```

**预期日志：**
```
D/DishRepository: === Starting sync from cloud ===
D/DishRemoteDataSource: === Starting to fetch dishes from Supabase ===
D/DishRemoteDataSource: Result length: 234
D/DishRemoteDataSource: Parsed JSON type: JsonArray
D/DishRepository: ✓ Inserted dish from cloud: 宫保鸡丁
D/DishRepository: === Sync completed. Synced 2 dishes ===
```

### 步骤 4: 测试删除同步

```bash
# 清除日志
adb logcat -c

# 删除一个菜品，观察日志
adb logcat | grep -E "deleteDish|DeleteDish"
```

**预期日志：**
```
D/DishListViewModel: === User triggered delete dish ===
D/DishRepository: === Starting delete dish ===
D/DishRemoteDataSource: === Starting to delete dish from Supabase ===
D/DishRemoteDataSource: Update data: {"is_deleted":true,...}
D/DishRemoteDataSource: === Dish deleted successfully in Supabase ===
D/DishRepository: ✓ Successfully synced to cloud
D/DishRepository: === Delete dish completed successfully ===
```

### 步骤 5: 验证云端数据

在 **Supabase Dashboard** → **SQL Editor** 中运行：

```sql
-- 查看已删除的菜品
SELECT id, name, is_deleted, updated_at
FROM dishes
WHERE is_deleted = true
ORDER BY updated_at DESC;

-- 查看活跃的菜品
SELECT id, name, is_deleted, updated_at
FROM dishes
WHERE is_deleted = false
ORDER BY updated_at DESC;
```

---

## 📊 诊断检查清单

### 数据同步问题

- [ ] GitHub Actions 构建成功（绿色 ✅）
- [ ] 在 Supabase 中运行了数据修复脚本
- [ ] 在 Supabase 中禁用了 RLS（或配置了正确策略）
- [ ] 卸载了旧版本应用
- [ ] 安装了新版本 APK
- [ ] 查看了同步日志
- [ ] 日志显示 "Sync completed"
- [ ] Supabase Dashboard 能看到数据
- [ ] 应用中能看到云端数据

### 删除同步问题

- [ ] 查看了删除操作日志
- [ ] 日志显示 "Dish deleted successfully in Supabase"
- [ ] Supabase 中 is_deleted 被设置为 true
- [ ] 本地菜品消失
- [ ] 其他设备打开应用也看不到被删除的菜品

---

## 📚 相关文档

- **数据同步调试**: `docs/DEBUG_SYNC_ISSUE.md`
- **删除操作调试**: `docs/DEBUG_DELETE_ISSUE.md`
- **卸载重装指南**: `docs/ANDROID_STUDIO_GUIDE.md`
- **云端同步修复**: `docs/CLOUD_SYNC_FIX.md`
- **完整修复总结**: `docs/FINAL_FIX_SUMMARY.md`

---

## 🎯 如果问题仍然存在

### 发送以下信息：

1. **数据同步日志**
   ```bash
   adb logcat -d | grep -E "DishRepository|DishRemoteDataSource" > sync_log.txt
   ```

2. **删除操作日志**
   ```bash
   adb logcat -d | grep -E "deleteDish|DeleteDish" > delete_log.txt
   ```

3. **Supabase 查询结果**
   ```sql
   -- 菜品数据
   SELECT * FROM dishes ORDER BY updated_at DESC LIMIT 5;

   -- RLS 策略
   SELECT * FROM pg_policies WHERE tablename = 'dishes';

   -- 表结构
   \d dishes
   ```

4. **应用信息**
   - APK 构建时间
   - 最新 commit hash
   - Android 版本
   - 设备型号

---

**最后更新：** 2026-03-29
**包含调试日志的版本：** commit `b5f1241`
