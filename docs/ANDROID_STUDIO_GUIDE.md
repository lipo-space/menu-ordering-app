# 📱 在 Android Studio 中卸载和重装应用

## 方法 1：通过 Android Studio（推荐）

### 步骤 1：打开 Android Studio
1. 打开你的 Android Studio 项目
2. 等待项目加载完成

### 步骤 2：找到设备管理器
1. 在 Android Studio 底部工具栏，点击 **Logcat** 标签
2. 在 Logcat 窗口的顶部，你会看到当前连接的设备

### 步骤 3：卸载应用（方法 A - 通过 UI）
1. 在 Logcat 窗口右上角，找到设备下拉菜单旁边的下拉箭头
2. 或者直接在设备上长按应用图标 → 卸载

### 步骤 4：卸载应用（方法 B - 通过命令行）
在 Android Studio 底部的 **Terminal** 中运行：

```bash
# 卸载应用
adb uninstall com.lipo.menu

# 如果提示 "Success"，说明卸载成功
```

### 步骤 5：重新安装应用
1. 点击 Android Studio 顶部的绿色 **Run** 按钮（▶️）
2. 或者按快捷键 `Shift + F10` (Windows/Linux) 或 `Control + R` (Mac)
3. 选择你的设备
4. 等待安装完成

---

## 方法 2：通过设备设置（完全清除数据）

### Android 设备上操作：

1. **打开设置**
   - Settings → Apps → Menu
   - 或者：Settings → Applications → Menu

2. **清除数据和卸载**
   - 点击 **Storage**
   - 点击 **Clear Data** （清除数据）
   - 点击 **Clear Cache** （清除缓存）
   - 返回上一级
   - 点击 **Uninstall** （卸载）

3. **重新安装**
   - 在 Android Studio 中点击 Run 按钮
   - 或者手动安装 APK 文件

---

## 方法 3：通过 adb 命令（最彻底）

在 Android Studio 的 Terminal 或系统终端中运行：

```bash
# 1. 查看连接的设备
adb devices

# 2. 卸载应用（保留数据）
adb uninstall -k com.lipo.menu

# 3. 完全卸载应用（删除所有数据）- 推荐
adb uninstall com.lipo.menu

# 4. 清除设备上的应用数据缓存
adb shell pm clear com.lipo.menu

# 5. 验证应用已卸载
adb shell pm list packages | grep com.lipo.menu
# 应该没有输出

# 6. 重新安装（在 Android Studio 中点击 Run）
```

---

## 🔧 为什么必须卸载重装？

### 问题原因：
1. **旧版本没有同步功能** - 旧代码没有 `syncFromCloud()` 方法
2. **本地数据库冲突** - 旧数据可能阻止新数据同步
3. **代码更新** - 新的 upsert 逻辑使用 `onConflict` 参数

### 卸载重装的作用：
- ✅ 清除旧的本地数据库
- ✅ 清除旧的 SharedPreferences
- ✅ 确保使用最新的代码版本
- ✅ 强制从云端重新拉取数据

---

## 📊 验证应用版本

### 检查当前安装的版本：

```bash
# 查看应用版本
adb shell dumpsys package com.lipo.menu | grep versionName

# 查看应用信息
adb shell dumpsys package com.lipo.menu | head -20
```

### 在应用中查看：
- 打开应用
- 查看 Settings 或 About 页面
- 应该显示最新的版本号

---

## 🐛 常见问题

### 问题 1：adb 命令找不到
**解决方案：** 使用 Android Studio 自带的 Terminal，它会自动配置 adb 路径

### 问题 2：设备未授权
**解决方案：**
1. 在设备上查看是否弹出授权对话框
2. 勾选 "Always allow from this computer"
3. 点击 "Allow"

### 问题 3：卸载失败
**错误：** `DELETE_FAILED_INTERNAL_ERROR`

**解决方案：**
```bash
# 1. 先停止应用
adb shell am force-stop com.lipo.menu

# 2. 再卸载
adb uninstall com.lipo.menu

# 3. 如果还不行，重启设备后重试
```

### 问题 4：安装失败 - INSTALL_FAILED_UPDATE_INCOMPATIBLE
**原因：** 签名不匹配

**解决方案：**
```bash
# 必须先卸载旧版本
adb uninstall com.lipo.menu

# 然后重新安装
```

---

## 📱 完整操作流程（推荐）

### 第一步：在 Supabase 中修复数据

在 **Supabase Dashboard** → **SQL Editor** 中运行：

```sql
-- 修复 is_deleted 为 null 的记录
UPDATE dishes SET is_deleted = false WHERE is_deleted IS NULL;

-- 修复 today_menus
UPDATE today_menus SET created_at = NOW() WHERE created_at IS NULL;
UPDATE today_menus SET updated_at = NOW() WHERE updated_at IS NULL;

-- 修复 dishes 的 updated_at
UPDATE dishes SET updated_at = NOW() WHERE updated_at IS NULL;

-- 设置默认值
ALTER TABLE dishes ALTER COLUMN is_deleted SET DEFAULT false;
ALTER TABLE today_menus ALTER COLUMN created_at SET DEFAULT NOW();
ALTER TABLE today_menus ALTER COLUMN updated_at SET DEFAULT NOW();

-- 验证
SELECT COUNT(*) as null_count FROM dishes WHERE is_deleted IS NULL;
-- 应该返回 0
```

### 第二步：在 Android Studio 中卸载应用

在 Android Studio Terminal 中：

```bash
# 卸载应用
adb uninstall com.lipo.menu

# 确认卸载成功
adb shell pm list packages | grep com.lipo.menu
# 应该没有输出
```

### 第三步：获取最新代码

在 Android Studio Terminal 中：

```bash
# 拉取最新代码
git pull origin main

# 或者查看当前状态
git status
```

### 第四步：重新编译和安装

1. 在 Android Studio 中点击 **Build** → **Clean Project**
2. 点击 **Build** → **Rebuild Project**
3. 点击绿色 **Run** 按钮（▶️）或按 `Shift + F10`
4. 选择你的设备
5. 等待安装完成

### 第五步：验证数据同步

1. **打开应用**
2. **查看 Logcat 日志：**

在 Android Studio 底部的 **Logcat** 标签中，过滤：
```
DishRepository
```

应该看到：
```
D/DishRepository: Starting sync from cloud
D/DishRemoteDataSource: Fetching dishes from Supabase
D/DishRemoteDataSource: Fetched dishes successfully
D/DishRemoteDataSource: Parsed 2 dishes from Supabase
D/DishRepository: Inserted dish from cloud: 宫保鸡丁
D/DishRepository: Sync completed. Synced 2 dishes
```

3. **检查应用界面**
   - 应该能看到其他设备添加的菜品

4. **在 Supabase Dashboard 中验证**
   - Table Editor → dishes
   - 应该能看到所有菜品，且 `is_deleted = false`

---

## 🎯 测试家庭共享

### 在设备 A 上：
1. 打开应用
2. 添加一个新菜品（例如：麻婆豆腐）
3. 等待 5 秒

### 在设备 B 上：
1. **卸载旧版本**（如果还没有）
2. **安装新版本**
3. 打开应用
4. **应该能看到：**
   - 之前所有共享的菜品
   - 设备 A 刚添加的"麻婆豆腐"

### 在 Supabase Dashboard 中：
1. 打开 Table Editor → dishes
2. 应该能看到所有菜品
3. `is_deleted` 应该全部是 `false`

---

## 🔍 调试技巧

### 实时查看日志：
```bash
# 清除旧日志
adb logcat -c

# 实时查看同步日志
adb logcat | grep -E "DishRepository|DishRemoteDataSource|TodayMenuRepository"

# 或者查看所有应用日志
adb logcat | grep "com.lipo.menu"
```

### 检查网络请求：
```bash
# 查看网络相关日志
adb logcat | grep -E "Supabase|ktor|OkHttp"
```

### 检查数据库：
```bash
# 进入设备 shell
adb shell

# 进入应用数据库目录（需要 root 或 debuggable app）
run-as com.lipo.menu
cd databases
ls -la
```

---

## ⚠️ 重要提示

1. **必须在所有设备上卸载重装** - 只在一台设备上操作是不够的
2. **确保运行了 SQL 修复脚本** - 否则 `is_deleted = null` 的记录无法被查询到
3. **等待 GitHub Actions 构建完成** - 确保使用最新的 APK
4. **检查网络连接** - 设备需要能访问 Supabase 服务器

---

## 📋 检查清单

在测试前，确认以下所有项目：

- [ ] 在 Supabase 中运行了 SQL 修复脚本
- [ ] 验证 `SELECT COUNT(*) FROM dishes WHERE is_deleted IS NULL` 返回 0
- [ ] 在设备 A 上卸载了旧版本应用
- [ ] 在设备 A 上安装了新版本应用
- [ ] 在设备 B 上卸载了旧版本应用
- [ ] 在设备 B 上安装了新版本应用
- [ ] 在设备 A 上添加菜品成功
- [ ] 在 Supabase Dashboard 中能看到菜品
- [ ] 在设备 B 上能看到设备 A 的菜品
- [ ] Logcat 显示同步成功的日志

---

**最后更新：** 2026-03-29
**适用版本：** v1.0+
