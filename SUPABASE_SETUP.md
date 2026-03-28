# Supabase 云端同步设置指南

## 概述

这个应用使用 Supabase 作为云端数据库，让你的家庭成员可以共享同一份菜单数据。

## 第一步：在 Supabase 中创建数据库表

1. 访问你的 Supabase Dashboard：https://supabase.com/dashboard
2. 选择你的项目（或创建一个新项目）
3. 点击左侧菜单的 "SQL Editor"
4. 复制 `supabase_schema.sql` 文件中的所有内容
5. 粘贴到 SQL Editor 并点击 "Run"

这将创建以下表：
- `dishes` - 菜品表
- `today_menus` - 每日菜单表
- `today_menu_dishes` - 菜单-菜品关联表
- `dish_combinations` - 菜品搭配表
- `combination_dishes` - 搭配-菜品关联表

## 第二步：获取你的 Supabase 凭证

1. 在 Supabase Dashboard，点击左侧 "Settings" (齿轮图标)
2. 点击 "API"
3. 复制以下两个值：
   - **Project URL** (类似 `https://xxxxx.supabase.co`)
   - **anon public key** (一个很长的 JWT token)

## 第三步：配置应用

你的应用已经包含了 Supabase 配置，凭证在：
`app/src/main/java/com/lipo/menu/data/remote/SupabaseConfig.kt`

**重要：** 对于家庭使用，当前的配置已经可以工作了。如果你需要使用自己的 Supabase 项目：

1. 打开 `SupabaseConfig.kt`
2. 替换 `SUPABASE_URL` 和 `SUPABASE_ANON_KEY` 为你的值
3. **不要** 将这些值提交到公开的 Git 仓库

## 第四步：构建并安装应用

```bash
# 构建应用
./gradlew assembleDebug

# APK 文件位置
app/build/outputs/apk/debug/app-debug.apk
```

或者从 GitHub Actions 下载最新的 APK。

## 第五步：安装到家人手机

1. 传输 APK 到家人的手机
2. 在手机上打开 APK 文件
3. 允许"安装未知来源应用"
4. 安装完成

## 如何工作

- **本地优先**：应用首先保存数据到本地数据库，确保离线也能使用
- **后台同步**：在后台自动将数据同步到 Supabase
- **实时更新**：当其他家庭成员添加或修改菜品时，你的手机会自动更新
- **冲突解决**：使用 `updated_at` 时间戳解决数据冲突

## 故障排查

### 同步不工作
1. 检查网络连接
2. 查看 Logcat 中的 "DishRemoteDataSource" 标签
3. 确认 Supabase 凭证正确

### 数据没有出现在其他设备
1. 确保两个设备使用相同的 Supabase 项目
2. 检查 Supabase Dashboard 的 Table Editor 查看数据是否已上传
3. 重启应用触发数据刷新

## 费用说明

Supabase 免费版包含：
- 500MB 数据库存储
- 1GB 文件存储
- 2GB 带宽/月
- 无限 API 请求

对于家庭菜单应用，免费版完全足够！

## 安全建议

1. **不要** 将 `SUPABASE_ANON_KEY` 提交到公开仓库
2. 考虑启用 Row Level Security (RLS) 以增强安全性
3. 定期在 Supabase Dashboard 备份数据

## 下一步

完成设置后，你的家人可以：
1. 在各自手机上安装应用
2. 添加菜品会自动同步到所有人
3. 每天选择菜单时看到相同的菜品列表

享受共享菜单管理吧！🍽️
