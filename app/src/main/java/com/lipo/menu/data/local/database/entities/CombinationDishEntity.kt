package com.lipo.menu.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "combination_dishes",
    primaryKeys = ["combination_id", "dish_id"],
    foreignKeys = [
        ForeignKey(
            entity = CombinationEntity::class,
            parentColumns = ["id"],
            childColumns = ["combination_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dish_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["combination_id"]),
        Index(value = ["dish_id"])
    ]
)
data class CombinationDishEntity(
    @ColumnInfo(name = "combination_id")
    val combinationId: String,

    @ColumnInfo(name = "dish_id")
    val dishId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
