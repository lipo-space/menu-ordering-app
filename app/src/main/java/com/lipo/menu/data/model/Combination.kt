package com.lipo.menu.data.model

import java.time.Instant

data class Combination(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val dishes: List<Dish> = emptyList()
)
