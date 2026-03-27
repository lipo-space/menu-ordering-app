package com.lipo.menu.data.local.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Represents a Combination with all its associated Dishes.
 * Used for querying combinations with their dishes in a single query.
 */
data class CombinationWithDishes(
    @Embedded val combination: CombinationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CombinationDishEntity::class,
            parentColumn = "combination_id",
            entityColumn = "dish_id"
        )
    )
    val dishes: List<DishEntity>
)
