# 📊 当前状态和下一步操作

## ✅ 已完成的修复

### 1. 修复 isNull 编译错误
**问题：** `Unresolved reference: isNull`

**原因：** Supabase-kt 2.x 的 filter DSL 不支持 `isNull()` 函数

**解决方案：**
```kotlin
// 之前的代码（不兼容）
filter {
    or {
        eq("is_deleted", false)
        isNull("is_deleted")  // ❌ 不支持
    }
}

// 修复后的代码
filter {
    eq("is_deleted", false)  // ✅ 简单可靠
}
```

**提交记录：** `96f1fdf` - docs: 添加 Supabase 快速修复指南

### 2. 修复 UpsertOptions 编译错误 ✅ 新修复
**问题：** `Unresolved reference: UpsertOptions`

**原因：** Supabase-kt 2.x 不使用 `UpsertOptions` 类，`onConflict` 应该作为 `upsert()` 函数的直接参数

**解决方案：**
```kotlin
// 之前的代码（错误）
import io.github.jan.supabase.postgrest.UpsertOptions  // ❌ 不存在

client.from("dishes").upsert(
    JsonObject(...),
    upsertOptions = UpsertOptions(onConflict = "id")  // ❌ 错误
)

// 修复后的代码（正确）
// 移除 UpsertOptions 导入

client.from("dishes").upsert(
    JsonObject(...),
    onConflict = "id"  // ✅ 正确
)
```

**同时修复的问题：**
- ✅ today_menus 重复键错误（使用 `onConflict = "date"`）
- ✅ today_menu_dishes 复合主键冲突（使用 `onConflict = "today_menu_id,dish_id"`）

**提交记录：** `5258fc2` - fix: 修复 UpsertOptions 编译错误

### 3. 已推送到 GitHub
代码已推送到 `main` 分支，GitHub Actions 正在自动构建新的 APK。

---

## 🔧 你需要做的操作（按顺序）

### 第一步：修复 Supabase 数据库中的 null 值

1. 打开 [Supabase Dashboard](https://supabase.com/dashboard)
2. 选择你的项目
3. 点击左侧 **SQL Editor**
4. 复制并运行以下 SQL：

```sql
-- ============================================
-- 修复数据库中的 null 值问题
-- ============================================

-- 1. 修复 is_deleted 为 null 的记录
UPDATE dishes
SET is_deleted = false
WHERE is_deleted IS NULL;

-- 2. 修复 today_menus 的时间戳
UPDATE today_menus
SET created_at = NOW()
WHERE created_at IS NULL;

UPDATE today_menus
SET updated_at = NOW()
WHERE updated_at IS NULL;

-- 3. 修复 dishes 的时间戳
UPDATE dishes
SET updated_at = NOW()
WHERE updated_at IS NULL;

-- 4. 设置默认值（防止未来出现 null）
ALTER TABLE dishes
ALTER COLUMN is_deleted SET DEFAULT false;

ALTER TABLE today_menus
ALTER COLUMN created_at SET DEFAULT NOW();

ALTER TABLE today_menus
ALTER COLUMN updated_at SET DEFAULT NOW();

-- 5. 验证修复结果
SELECT
    COUNT(*) as total_dishes,
    COUNT(CASE WHEN is_deleted = false THEN 1 END) as active_dishes,
    COUNT(CASE WHEN is_deleted IS NULL THEN 1 END) as null_deleted
FROM dishes;
```

**预期结果：** `null_deleted` 应该为 `0`

---

### 第二步：等待 GitHub Actions 构建完成

1. 打开你的 GitHub 仓库
2. 点击 **Actions** 标签
3. 查看最新的 workflow 运行状态
4. 等待显示 ✅ 绿色对勾（约 3-5 分钟）

**构建状态：**
- 🟡 黄色圆圈 = 正在构建
- ✅ 绿色对勾 = 构建成功
- ❌ 红色叉号 = 构建失败（需要查看日志）

---

### 第三步：下载并安装新版本 APK

**重要：必须先卸载旧版本！**

1. **卸载旧版本应用**
   - 长按应用图标
   - 选择"卸载"
   - 确认卸载

   **为什么要卸载？**
   - 清除旧的本地数据库
   - 确保从云端重新同步数据
   - 避免数据冲突

2. **下载新版本 APK**
   - 在 GitHub Actions 页面
   - 点击已完成的 workflow
   - 在 "Artifacts" 部分下载 APK
   - 或者从 Releases 页面下载

3. **安装新 APK**
   - 打开下载的 APK
   - 允许安装未知来源应用（如果提示）
   - 完成安装

---

### 第四步：测试家庭共享功能

#### 在设备 A 上：
1. 打开应用
2. 添加菜品（例如：宫保鸡丁、红烧肉）
3. 等待 3-5 秒（数据同步到云端）
4. 打开 Supabase Dashboard → Table Editor → dishes
5. 确认能看到刚才添加的菜品

#### 在设备 B 上：
1. **卸载旧版本**（如果有）
2. 安装新的 APK
3. 打开应用
4. **应该能看到设备 A 添加的菜品** ✅

#### 验证日志：
```bash
# 在设备 B 上查看日志
adb logcat | grep -E "DishRepository|DishRemoteDataSource"
```

**成功的日志应该显示：**
```
D/DishRemoteDataSource: Fetching dishes from Supabase
D/DishRemoteDataSource: Fetched dishes successfully
D/DishRemoteDataSource: Parsed 2 dishes from Supabase
D/DishRepository: Starting sync from cloud
D/DishRepository: Inserted dish from cloud: 宫保鸡丁
D/DishRepository: Inserted dish from cloud: 红烧肉
D/DishRepository: Sync completed. Synced 2 dishes
```

---

## 🎯 工作原理

### 数据同步流程

```
设备 A 添加菜品
    ↓
保存到本地数据库 (Room)
    ↓
同步到云端数据库 (Supabase)
    ↓
    [云端存储]
    ↓
设备 B 打开应用
    ↓
从云端获取数据 (fetchAllDishes)
    ↓
保存到本地数据库
    ↓
UI 显示共享数据 ✅
```

### 关键技术点

1. **云端查询条件：**
   ```kotlin
   filter {
       eq("is_deleted", false)  // 只获取未删除的菜品
   }
   ```

2. **SQL 数据修复：**
   ```sql
   UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;
   ```
   这确保所有数据都符合查询条件。

3. **智能合并：**
   - 基于时间戳保留最新数据
   - 本地不存在 → 插入
   - 云端更新 → 更新本地

---

## 🐛 故障排查

### 问题 1：GitHub Actions 构建失败

**检查步骤：**
1. 点击失败的 workflow
2. 查看 "Build" 步骤的日志
3. 查找错误信息

**常见原因：**
- 依赖版本问题
- 编译错误
- ProGuard 配置问题

### 问题 2：设备 B 看不到设备 A 的数据

**检查清单：**
- [ ] 设备 A 成功添加菜品
- [ ] Supabase Dashboard 能看到数据
- [ ] SQL 修复脚本已运行
- [ ] 设备 B 已卸载旧版本
- [ ] 设备 B 安装了新版本 APK
- [ ] 查看日志确认同步成功

**调试命令：**
```bash
# 查看同步日志
adb logcat | grep "DishRepository"

# 查看 Supabase 数据
# 在 SQL Editor 中运行：
SELECT * FROM dishes WHERE is_deleted = false ORDER BY created_at DESC;
```

### 问题 3：应用闪退

**可能原因：**
- 旧版本数据冲突
- 数据库迁移问题

**解决方案：**
1. 完全卸载应用
2. 清除设备缓存
3. 重新安装 APK

---

## 📚 相关文档

- **快速修复指南：** `docs/SUPABASE_QUICK_FIX.md`
- **调试同步问题：** `docs/DEBUG_SYNC.md`
- **数据同步指南：** `docs/DATA_SYNC_GUIDE.md`
- **实现总结：** `docs/IMPLEMENTATION_SUMMARY.md`

---

## ✅ 完成标志

当你看到以下情况时，说明一切正常：

- [x] 代码已修复并推送到 GitHub
- [ ] GitHub Actions 构建成功（绿色对勾）
- [ ] SQL 修复脚本已运行
- [ ] 新 APK 已下载
- [ ] 旧版本已卸载
- [ ] 新 APK 已安装
- [ ] 设备 A 添加菜品成功
- [ ] Supabase 能看到数据
- [ ] 设备 B 能看到设备 A 的数据

---

## 🎉 预期结果

完成以上步骤后，你的家庭应该能够：

1. ✅ 在任何设备上添加菜品
2. ✅ 数据自动同步到云端
3. ✅ 其他设备启动应用时自动获取共享数据
4. ✅ 实时共享今日菜单
5. ✅ 所有操作支持离线使用（本地优先）

---

**下一步改进建议：**
- 添加实时推送通知（Supabase Realtime）
- 实现更完善的冲突解决策略
- 添加数据同步状态指示器
- 支持多用户（用户认证）

---

**最后更新：** 2026-03-28
**修复版本：** commit `96f1fdf`
