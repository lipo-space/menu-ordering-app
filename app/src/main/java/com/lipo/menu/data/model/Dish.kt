package com.lipo.menu.data.model

import java.time.Instant

data class Dish(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean = false
)
