package com.lipo.menu.domain.validator

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMenuValidator @Inject constructor() {

    fun validateMenuDate(date: LocalDate) {
        val today = LocalDate.now()

        // Allow creating menus for today or past dates (for historical purposes)
        // But prevent creating menus for future dates beyond 1 day
        if (date.isAfter(today.plusDays(1))) {
            throw ValidationException("Menu date cannot be more than 1 day in the future")
        }
    }

    fun validateDishSelection(dishIds: List<String>) {
        if (dishIds.isEmpty()) {
            throw ValidationException("Menu must have at least one dish")
        }

        // Check for duplicates
        val uniqueDishIds = dishIds.toSet()
        if (uniqueDishIds.size != dishIds.size) {
            throw ValidationException("Menu cannot contain duplicate dishes")
        }

        // Validate each dish ID is not blank
        dishIds.forEach { dishId ->
            if (dishId.isBlank()) {
                throw ValidationException("Dish ID cannot be blank")
            }
        }
    }
}
