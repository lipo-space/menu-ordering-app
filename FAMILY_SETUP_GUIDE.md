# 🏠 家庭菜单应用 - 快速设置指南

## ✅ 已完成的工作

1. ✅ 启用了 Supabase 云端同步功能
2. ✅ 配置好了云端数据库
3. ✅ 代码已推送到 GitHub
4. ✅ CI/CD 正在构建新的 APK

---

## 📋 你需要做的事情（5分钟）

### 第一步：创建 Supabase 数据库表（必做）

1. 打开浏览器访问：https://supabase.com/dashboard
2. 登录你的账号
3. 选择项目（URL 中包含 `yrredllhwswsfxvzhoka`）
4. 点击左侧菜单 **"SQL Editor"**
5. 点击 **"New Query"**
6. 打开项目中的 `supabase_schema.sql` 文件
7. 复制所有内容
8. 粘贴到 SQL Editor
9. 点击右下角 **"Run"** 按钮
10. 看到 "Success. No rows returned" 表示成功

### 第二步：下载并安装 APK

**方式 A：从 GitHub Actions 下载（推荐）**

1. 等待 5-10 分钟让 CI 构建完成
2. 访问：https://github.com/lipo-space/menu-ordering-app/actions
3. 点击最新的成功构建（绿色 ✓）
4. 滚动到底部 "Artifacts"
5. 下载 `debug-apk`
6. 解压得到 `app-debug.apk`

**方式 B：在 Android Studio 中构建**

```bash
./gradlew assembleDebug
# APK 位置：app/build/outputs/apk/debug/app-debug.apk
```

### 第三步：安装到手机

1. 将 APK 传输到手机（微信/邮件/数据线）
2. 在手机上点击 APK 文件
3. 如果提示"禁止安装"，去设置允许
4. 安装完成

### 第四步：分享给家人

将同一个 APK 文件发给家人，让他们也安装。

---

## 🎉 完成！

现在你和家人可以：
- ✅ 在各自手机上添加菜品
- ✅ 实时同步到所有人的手机
- ✅ 看到相同的菜单列表
- ✅ 每天一起选择今天吃什么

---

## 🔧 工作原理

```
你的手机 → 本地数据库 → Supabase 云端 ← 家人的手机
     ↑                                        ↓
     └──────────── 实时同步 ←─────────────────┘
```

- **离线支持**：没有网络时，数据保存在本地，有网络后自动同步
- **实时更新**：家人添加菜品，你的手机会立即更新
- **数据安全**：所有数据都保存在你自己的 Supabase 项目中

---

## 💰 费用

**完全免费！** Supabase 免费版包含：
- 500MB 数据库（足够存储数千个菜品）
- 无限 API 请求
- 实时同步功能

---

## ❓ 常见问题

**Q: 如何知道同步是否成功？**
A: 在 Supabase Dashboard 的 Table Editor 中可以看到所有数据

**Q: 数据会丢失吗？**
A: 不会！数据同时保存在本地和云端，双重保险

**Q: 需要登录吗？**
A: 目前使用默认用户，未来可以添加登录功能

**Q: 可以在 iPhone 上使用吗？**
A: 目前只有 Android 版本，未来可以开发 iOS 版本

---

## 📞 需要帮助？

如果遇到问题，检查：
1. 网络连接是否正常
2. Supabase Dashboard 中是否有数据
3. 手机是否允许应用访问网络

---

**享受家庭共享菜单吧！** 🍽️👨‍👩‍👧‍👦
