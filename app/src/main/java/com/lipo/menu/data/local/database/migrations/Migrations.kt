package com.lipo.menu.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create today_menus table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS today_menus (
                id TEXT NOT NULL PRIMARY KEY,
                date TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Create unique index on date
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_today_menus_date ON today_menus(date)
            """.trimIndent()
        )

        // Create today_menu_dishes junction table with foreign keys
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS today_menu_dishes (
                today_menu_id TEXT NOT NULL,
                dish_id TEXT NOT NULL,
                `order` INTEGER NOT NULL,
                PRIMARY KEY(today_menu_id, dish_id),
                FOREIGN KEY(today_menu_id) REFERENCES today_menus(id) ON DELETE CASCADE,
                FOREIGN KEY(dish_id) REFERENCES dishes(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        // Create indices for foreign keys
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_today_menu_dishes_today_menu_id ON today_menu_dishes(today_menu_id)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_today_menu_dishes_dish_id ON today_menu_dishes(dish_id)
            """.trimIndent()
        )
    }
}
