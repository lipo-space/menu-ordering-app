package com.lipo.menu.data.local.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Represents a TodayMenu with all its associated Dishes.
 * Used for querying today menus with their dishes in a single query.
 */
data class TodayMenuWithDishes(
    @Embedded val todayMenu: TodayMenuEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TodayMenuDishEntity::class,
            parentColumn = "today_menu_id",
            entityColumn = "dish_id"
        )
    )
    val dishes: List<DishEntity>
)
