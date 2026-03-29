# 📱 家庭共享数据同步指南

## ✅ 已实现的功能

### 自动云端同步
- ✅ **添加菜品** → 自动同步到云端
- ✅ **更新菜品** → 自动同步到云端
- ✅ **删除菜品** → 自动同步到云端（软删除）
- ✅ **应用启动** → 自动从云端拉取所有家庭成员的数据

### 数据流向
```
设备 A 添加菜品 → 同步到 Supabase → 设备 B 启动应用 → 从云端拉取数据
```

## 🚀 使用步骤

### 第一步：配置 Supabase（必须）

1. 登录 [Supabase Dashboard](https://supabase.com/dashboard)
2. 选择你的项目
3. 点击左侧 **SQL Editor**
4. 运行以下 SQL：

```sql
-- 禁用 RLS 允许家庭共享
ALTER TABLE dishes DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menus DISABLE ROW LEVEL SECURITY;
ALTER TABLE today_menu_dishes DISABLE ROW LEVEL SECURITY;
```

### 第二步：安装新版本 APK

1. 等待 GitHub Actions 构建完成（约3分钟）
2. 下载最新的 APK
3. **卸载旧版本应用**（重要！需要清除本地数据库）
4. 安装新 APK

### 第三步：测试家庭共享

1. **在设备 A 上**：
   - 打开应用
   - 添加几个菜品（例如：宫保鸡丁、红烧肉）
   - 等待同步完成（通常几秒钟）

2. **在设备 B 上**：
   - 安装相同的 APK
   - 打开应用
   - **应该能看到设备 A 添加的菜品**

3. **验证数据**：
   - 打开 Supabase Dashboard
   - 进入 **Table Editor** → **dishes**
   - 应该能看到所有添加的菜品

## 🔄 同步机制

### 自动同步时机
1. **应用启动时** - 自动从云端拉取所有数据
2. **添加/修改/删除时** - 立即同步到云端

### 数据冲突处理
- **基于时间戳** - 保留最新修改的数据
- **云端优先** - 云端数据优先级高于本地

### 同步日志
查看同步日志：
```bash
adb logcat | grep -E "DishRepository|TodayMenuRepository"
```

成功的日志示例：
```
D/DishRepository: Starting sync from cloud
D/DishRepository: Inserted dish from cloud: 宫保鸡丁
D/DishRepository: Updated dish from cloud: 红烧肉
D/DishRepository: Sync completed. Synced 5 dishes
```

## 🐛 故障排查

### 问题 1：看不到其他设备的数据

**检查步骤：**

1. **验证 Supabase 数据**
   ```sql
   SELECT * FROM dishes WHERE is_deleted = false;
   ```
   如果有数据，说明数据已同步到云端

2. **检查 RLS 是否已禁用**
   ```sql
   SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname = 'public';
   ```
   应该显示 `rowsecurity = false`

3. **查看应用日志**
   ```bash
   adb logcat | grep "DishRepository"
   ```
   看是否有错误信息

4. **清除应用数据重试**
   - 卸载应用
   - 重新安装
   - 打开应用查看日志

### 问题 2：添加菜品失败

**可能原因：**
- 网络权限未添加 → 已修复
- 网络连接问题
- Supabase 服务问题

**检查日志：**
```bash
adb logcat | grep "DishRemoteDataSource"
```

### 问题 3：创建今日菜单失败

**原因：** `display_order` 列不存在

**解决方案：** 在 Supabase SQL Editor 中运行：
```sql
-- 检查表结构
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'today_menu_dishes';

-- 如果 display_order 不存在，手动添加
ALTER TABLE today_menu_dishes ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;
```

## 📊 数据库表结构

### dishes 表
| 列名 | 类型 | 说明 |
|------|------|------|
| id | TEXT | 主键，UUID |
| name | TEXT | 菜品名称 |
| description | TEXT | 描述（可为空）|
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |
| is_deleted | BOOLEAN | 是否已删除 |
| user_id | TEXT | 用户ID（默认 'default-user'）|

### today_menus 表
| 列名 | 类型 | 说明 |
|------|------|------|
| id | TEXT | 主键，UUID |
| date | DATE | 日期（唯一）|
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |
| user_id | TEXT | 用户ID |

### today_menu_dishes 表
| 列名 | 类型 | 说明 |
|------|------|------|
| today_menu_id | TEXT | 外键，关联 today_menus |
| dish_id | TEXT | 外键，关联 dishes |
| display_order | INTEGER | 显示顺序 |

## ✨ 最佳实践

1. **定期清理已删除数据**
   ```sql
   DELETE FROM dishes WHERE is_deleted = true;
   ```

2. **监控数据同步**
   - 定期检查 Supabase Dashboard
   - 查看应用日志

3. **备份数据**
   - Supabase 提供自动备份
   - 也可以导出数据

4. **网络优化**
   - 应用启动时同步，避免频繁同步
   - 使用 Wi-Fi 进行首次大容量同步

## 🎯 下一步改进

- [ ] 实现今日菜单的云端同步
- [ ] 添加实时推送通知（使用 Supabase Realtime）
- [ ] 实现离线模式支持
- [ ] 添加数据同步状态指示器

---

完成以上配置后，你的家庭就可以实时共享菜品数据了！🎉
