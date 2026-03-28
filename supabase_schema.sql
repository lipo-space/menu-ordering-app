-- ============================================
-- 菜单应用 Supabase 数据库表结构
-- ============================================
-- 在 Supabase Dashboard 的 SQL Editor 中执行此脚本
-- ============================================

-- 创建菜品表
CREATE TABLE IF NOT EXISTS dishes (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    user_id TEXT DEFAULT 'default-user'
);

-- 创建每日菜单表
CREATE TABLE IF NOT EXISTS today_menus (
    id TEXT PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    user_id TEXT DEFAULT 'default-user'
);

-- 创建菜单-菜品关联表
CREATE TABLE IF NOT EXISTS today_menu_dishes (
    today_menu_id TEXT NOT NULL REFERENCES today_menus(id) ON DELETE CASCADE,
    dish_id TEXT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (today_menu_id, dish_id)
);

-- 创建菜品搭配表
CREATE TABLE IF NOT EXISTS dish_combinations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    user_id TEXT DEFAULT 'default-user'
);

-- 创建搭配-菜品关联表
CREATE TABLE IF NOT EXISTS combination_dishes (
    combination_id TEXT NOT NULL REFERENCES dish_combinations(id) ON DELETE CASCADE,
    dish_id TEXT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (combination_id, dish_id)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_dishes_user_id ON dishes(user_id);
CREATE INDEX IF NOT EXISTS idx_dishes_is_deleted ON dishes(is_deleted);
CREATE INDEX IF NOT EXISTS idx_dishes_name ON dishes(name);

CREATE INDEX IF NOT EXISTS idx_today_menus_date ON today_menus(date);
CREATE INDEX IF NOT EXISTS idx_today_menus_user_id ON today_menus(user_id);

CREATE INDEX IF NOT EXISTS idx_dish_combinations_user_id ON dish_combinations(user_id);

-- 启用行级安全策略（RLS）
-- 对于家庭使用，我们可以暂时禁用 RLS 以简化配置
-- 如果需要更严格的安全控制，请参考 Supabase 文档启用 RLS

-- 插入示例数据（可选）
INSERT INTO dishes (id, name, description, created_at, updated_at, is_deleted, user_id)
VALUES
    ('sample-1', '宫保鸡丁', '经典川菜，微辣', NOW(), NOW(), FALSE, 'default-user'),
    ('sample-2', '红烧肉', '家常菜，肥而不腻', NOW(), NOW(), FALSE, 'default-user'),
    ('sample-3', '清炒时蔬', '新鲜蔬菜，清淡健康', NOW(), NOW(), FALSE, 'default-user')
ON CONFLICT (id) DO NOTHING;
