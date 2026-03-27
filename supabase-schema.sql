-- ========================================
-- 菜单应用数据库表结构
-- ========================================
-- 在 Supabase SQL Editor 中执行此脚本
-- ========================================

-- 1. 菜品表
CREATE TABLE IF NOT EXISTS dishes (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  user_id TEXT NOT NULL DEFAULT 'default-user'
);

-- 2. 搭配表
CREATE TABLE IF NOT EXISTS combinations (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  user_id TEXT NOT NULL DEFAULT 'default-user'
);

-- 3. 搭配菜品关联表
CREATE TABLE IF NOT EXISTS combination_dishes (
  combination_id TEXT NOT NULL REFERENCES combinations(id) ON DELETE CASCADE,
  dish_id TEXT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
  "order" INTEGER NOT NULL,
  PRIMARY KEY (combination_id, dish_id)
);

-- 4. 今日菜单表
CREATE TABLE IF NOT EXISTS today_menus (
  id TEXT PRIMARY KEY,
  date DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  user_id TEXT NOT NULL DEFAULT 'default-user'
);

-- 5. 今日菜单菜品关联表
CREATE TABLE IF NOT EXISTS today_menu_dishes (
  today_menu_id TEXT NOT NULL REFERENCES today_menus(id) ON DELETE CASCADE,
  dish_id TEXT NOT NULL REFERENCES dishes(id) ON DELETE RESTRICT,
  "order" INTEGER NOT NULL,
  PRIMARY KEY (today_menu_id, dish_id)
);

-- ========================================
-- 创建索引（提高查询性能）
-- ========================================

CREATE INDEX IF NOT EXISTS idx_dishes_user_id ON dishes(user_id);
CREATE INDEX IF NOT EXISTS idx_dishes_name ON dishes(name);
CREATE INDEX IF NOT EXISTS idx_combinations_user_id ON combinations(user_id);
CREATE INDEX IF NOT EXISTS idx_today_menus_user_id ON today_menus(user_id);
CREATE INDEX IF NOT EXISTS idx_today_menus_date ON today_menus(date);

-- ========================================
-- 配置 Row Level Security (RLS)
-- ========================================
-- 注意：由于暂时不需要用户认证，我们暂时禁用 RLS
-- 后续添加用户系统时再启用
-- ========================================

-- 暂时允许所有访问（开发阶段）
ALTER TABLE dishes ENABLE ROW LEVEL SECURITY;
ALTER TABLE combinations ENABLE ROW LEVEL SECURITY;
ALTER TABLE today_menus ENABLE ROW LEVEL SECURITY;

-- 创建允许所有访问的策略（开发阶段使用）
CREATE POLICY "Allow all access during development" ON dishes
  FOR ALL USING (true);

CREATE POLICY "Allow all access during development" ON combinations
  FOR ALL USING (true);

CREATE POLICY "Allow all access during development" ON today_menus
  FOR ALL USING (true);

-- ========================================
-- 验证表创建成功
-- ========================================
-- 执行以下查询验证表是否创建成功：
-- SELECT * FROM dishes LIMIT 1;
-- SELECT * FROM combinations LIMIT 1;
-- SELECT * FROM today_menus LIMIT 1;
