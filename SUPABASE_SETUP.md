# Supabase 数据库配置指南

## 📋 前提条件

- ✅ 已有 Supabase 账号
- ✅ 代码已推送到 GitHub（https://github.com/lipo-space/menu-ordering-app）

---

## 第一步：创建 Supabase 项目

1. **访问 Supabase Dashboard**
   - 打开 https://supabase.com/dashboard
   - 登录你的账号

2. **创建新项目**
   - 点击 "New Project" 按钮
   - 选择组织（Organization）
   - 填写项目信息：
     - **Name**: `menu-app`（或你喜欢的名称）
     - **Database Password**: 设置一个强密码（**务必保存好**）
     - **Region**: 选择离你最近的区域（推荐：Singapore）
   - 点击 "Create new project"
   - 等待约 2 分钟，项目创建完成

---

## 第二步：获取 API 密钥

1. **进入项目设置**
   - 在项目页面，点击左侧菜单的 ⚙️ **Settings**
   - 选择 **API** 选项

2. **记录以下信息**（稍后需要用到）
   ```
   Project URL: https://xxxxxx.supabase.co
   anon public: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   service_role: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

   ⚠️ **重要**：
   - `anon public` 密钥：可以在客户端使用
   - `service_role` 密钥：**保密！**只能在服务端使用

---

## 第三步：创建数据库表结构

1. **打开 SQL Editor**
   - 在左侧菜单点击 **SQL Editor**
   - 点击 **New query**

2. **执行建表脚本**
   - 打开项目中的 `supabase-schema.sql` 文件
   - 复制所有内容
   - 粘贴到 SQL Editor 中
   - 点击 **Run** 按钮（或按 `Ctrl + Enter`）

3. **验证表创建成功**
   - 在左侧菜单点击 **Table Editor**
   - 应该看到以下表：
     - ✅ dishes
     - ✅ combinations
     - ✅ combination_dishes
     - ✅ today_menus
     - ✅ today_menu_dishes

---

## 第四步：测试数据连接（可选）

在 SQL Editor 中执行以下测试：

```sql
-- 测试插入菜品
INSERT INTO dishes (id, name, description, created_at, updated_at, is_deleted, user_id)
VALUES (
  'test-dish-001',
  '宫保鸡丁',
  '经典川菜',
  NOW(),
  NOW(),
  false,
  'default-user'
);

-- 查询验证
SELECT * FROM dishes WHERE id = 'test-dish-001';

-- 清理测试数据
DELETE FROM dishes WHERE id = 'test-dish-001';
```

---

## 第五步：配置环境变量（为下一步准备）

创建一个临时文件保存配置信息（**不要提交到 Git**）：

```bash
# 在项目根目录创建 .env.local 文件（仅用于记录）
cat > .env.local << 'EOF'
# Supabase 配置
SUPABASE_URL=https://xxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
EOF

# 添加到 .gitignore（防止泄露）
echo ".env.local" >> .gitignore
```

---

## ✅ 完成检查清单

- [ ] Supabase 项目已创建
- [ ] 数据库表结构已创建（5个表）
- [ ] Row Level Security 已配置
- [ ] API 密钥已保存
- [ ] 环境变量文件已创建（.env.local）

---

## 下一步

完成 Supabase 配置后，下一步是：
1. 集成 Supabase SDK 到 Android 应用
2. 实现实时数据同步
3. 测试数据同步功能

---

## 常见问题

### Q1: 为什么 user_id 默认是 'default-user'？
**A**: 因为当前应用暂时不需要用户认证系统，所有数据都属于一个默认用户。后续添加用户系统时可以迁移数据。

### Q2: Row Level Security 为什么要设置为允许所有访问？
**A**: 这是开发阶段的临时配置。正式上线前会添加用户认证，届时会更新 RLS 策略。

### Q3: 数据库密码忘记了怎么办？
**A**: 可以在 Supabase Dashboard → Settings → Database 中重置密码。

---

## 参考链接

- [Supabase 官方文档](https://supabase.com/docs)
- [Supabase Kotlin SDK](https://github.com/supabase-community/supabase-kt)
- [Row Level Security 指南](https://supabase.com/docs/guides/auth/row-level-security)
