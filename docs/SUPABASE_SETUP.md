# Supabase 家庭共享配置指南

## 📋 前提条件

你的应用已经集成了 Supabase 云端同步功能！当你在应用中添加/修改/删除菜品或菜单时，数据会自动同步到 Supabase。

但是，家庭成员默认看不到共享数据，因为 **Supabase 的 RLS (Row Level Security)** 默认会阻止匿名访问。

## 🔧 配置步骤

### 方案一：禁用 RLS（最简单，适合家庭使用）

1. 登录 [Supabase Dashboard](https://supabase.com/dashboard)
2. 选择你的项目（URL: `https://yrredllhwswsfxvzhoka.supabase.co`）
3. 点击左侧 **Table Editor**
4. 对以下三个表分别操作：

   **表 1: dishes**
   - 点击表名 `dishes`
   - 点击表名右侧的 **三个点 (...)**
   - 选择 **Edit Table**
   - 找到 **RLS (Row Level Security)** 开关
   - **关闭** 这个开关

   **表 2: today_menus**
   - 重复上述步骤

   **表 3: today_menu_dishes**
   - 重复上述步骤

### 方案二：使用 SQL（更快）

1. 在 Supabase Dashboard 中，点击左侧 **SQL Editor**
2. 点击 **New query**
3. 粘贴以下 SQL：

```sql
-- 禁用 RLS 以允许匿名访问（适合家庭使用）
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menus DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menu_dishes DISABLE ROW LEVEL SECURITY;
```

4. 点击 **Run** 执行

### 方案三：配置公开访问策略（更安全，适合生产环境）

如果你想保持 RLS 启用但允许公开访问：

```sql
-- 启用 RLS
ALTER TABLE dishes ENABLE ROW LEVEL SECURITY;
ALTER TABLE today_menus ENABLE ROW LEVEL SECURITY;
ALTER TABLE today_menu_dishes ENABLE ROW LEVEL SECURITY;

-- 创建允许所有访问的策略
CREATE POLICY "Allow all access" ON dishes FOR ALL USING (true);
CREATE POLICY "Allow all access" ON today_menus FOR ALL USING (true);
CREATE POLICY "Allow all access" ON today_menu_dishes FOR ALL USING (true);
```

## ✅ 验证配置

### 1. 检查数据同步

在应用中添加一个菜品，然后：

1. 打开 Supabase Dashboard
2. 进入 **Table Editor** → **dishes**
3. 应该能看到刚才添加的菜品数据

### 2. 测试家庭共享

1. 在设备 A 上添加菜品
2. 在设备 B 上安装相同 APK
3. 在设备 B 上打开应用
4. 应该能看到设备 A 添加的菜品

## 🎯 已实现的云端同步功能

### 菜品管理 (DishRepository)
- ✅ **添加菜品** → 自动同步到 `dishes` 表
- ✅ **更新菜品** → 自动更新云端数据
- ✅ **删除菜品** → 软删除并同步到云端

### 今日菜单 (TodayMenuRepository)
- ✅ **创建菜单** → 自动同步到 `today_menus` 表
- ✅ **更新菜单** → 自动更新云端数据
- ✅ **删除菜单** → 自动删除云端数据
- ✅ **添加菜品到菜单** → 自动同步到 `today_menu_dishes` 表
- ✅ **从菜单移除菜品** → 自动删除云端关联

## 🔍 故障排查

### 问题：家庭成员看不到数据

**检查步骤：**

1. **验证 RLS 已禁用**
   ```sql
   SELECT tablename, rowsecurity
   FROM pg_tables
   WHERE schemaname = 'public';
   ```
   应该显示 `rowsecurity = false`

2. **检查数据是否存在**
   ```sql
   SELECT * FROM dishes WHERE is_deleted = false;
   ```

3. **查看应用日志**
   ```bash
   adb logcat | grep -E "DishRemoteDataSource|TodayMenuRemoteDataSource"
   ```

### 问题：数据同步失败

检查日志中的错误信息：
- `Failed to sync dish` - 菜品同步失败
- `Failed to sync today menu` - 菜单同步失败

**常见原因：**
- 网络连接问题
- Supabase URL 或 Key 错误
- RLS 策略阻止写入

## 📱 Supabase 配置信息

你的应用配置：
- **URL**: `https://yrredllhwswsfxvzhoka.supabase.co`
- **Anon Key**: 已配置（在 `SupabaseConfig.kt` 中）

## 🚀 下一步

1. **立即配置 RLS** - 选择上述三个方案之一
2. **在应用中添加菜品** - 测试同步功能
3. **在 Supabase Dashboard 查看数据** - 验证同步成功
4. **分享 APK 给家庭成员** - 开始共享菜单

配置完成后，你的家庭就可以实时共享菜品和菜单数据了！🎉
