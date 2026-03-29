# 🔧 Supabase 快速修复指南

## ❌ 编译错误已修复

**错误信息：**
```
Unresolved reference: isNull
```

**原因：**
Supabase-kt 2.x 的 filter DSL 不支持 `isNull()` 函数

**修复：**
移除了 `isNull` 过滤器，改用更简单的查询

---

## ✅ 灚急配置步骤（5分钟完成）

### 第一步：修复现有数据

在 **Supabase Dashboard** → **SQL Editor** 中运行：

```sql
-- 修复 is_deleted 为 null 的记录
UPDATE dishes
SET is_deleted = false
WHERE is_deleted IS NULL;

-- 修复 today_menus
UPDATE today_menus
SET created_at = NOW()
WHERE created_at IS NULL;

-- 修复 updated_at
UPDATE dishes
SET updated_at = NOW()
WHERE updated_at IS NULL;

UPDATE today_menus
SET updated_at = NOW()
WHERE updated_at IS NULL;

-- 设置默认值（可选，ALTER TABLE dishes
ALTER COLUMN is_deleted SET DEFAULT false;

ALTER TABLE today_menus
ALTER COLUMN created_at SET DEFAULT NOW();

ALTER TABLE today_menus
ALTER COLUMN updated_at SET DEFAULT NOW();
```

### 第二步：等待 GitHub Actions 构建完成

约 3 分钟，在 Actions 页面查看构建状态：
- ✅ 绿色 = 构建成功
- ❌ 红色 = 构建失败（修复后应该成功）

### 第三步：安装新版本 APK

1. 下载最新的 APK
2. **卸载旧版本**（重要！清除本地数据库）
3. 安装新 APK

### 第四步：测试家庭共享

1. **在设备 A 上**：
   - 打开应用
   - 添加菜品：宫保鸡丁、   - 等待 3 秒

2. **在设备 B 上**：
   - 打开应用
   - **应该能看到宫保鸡丁** ✅

3. **验证 Supabase 数据**：
   - 打开 Supabase Dashboard
   - Table Editor → dishes
   - 应该能看到刚才添加的菜品

## 🎯 錾要提示

如果你之前运行过 `docs/DEBUG_SYNC.md` 中的 SQL 脚本，请重新运行这个快速修复版本！

这个版本更简单、更可靠：
- ✅ 移除了不兼容的 `isNull` 过滤器
- ✅ 使用标准的 `eq` 过滤器
- ✅ 在 SQL 中修复数据，不再依赖代码处理

## 📊 韥道

**之前的代码：**
```kotlin
filter {
    or {
        eq("is_deleted", false)
        isNull("is_deleted")  // ❌ 不兼容
    }
}
```

**新的代码：**
```kotlin
filter {
    eq("is_deleted", false)  // ✅ 简单可靠
}
```

**配合的 SQL 修复：**
```sql
UPDATE dishes
SET is_deleted = false
WHERE is_deleted IS NULL;
```

这样确保所有数据都符合查询条件！🎉
