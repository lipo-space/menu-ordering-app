package com.lipo.menu.domain.validator

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinationValidator @Inject constructor() {

    fun validateName(name: String): Result<String> {
        if (name.isBlank()) {
            throw ValidationException("Combination name cannot be empty or blank")
        }
        if (name.length > 100) {
            throw ValidationException("Combination name cannot exceed 100 characters")
        }
        return Result.success(name.trim())
    }

    fun validateDescription(description: String?): Result<String?> {
        if (description != null && description.length > 500) {
            throw ValidationException("Description cannot exceed 500 characters")
        }
        return Result.success(description?.trim())
    }

    fun validateDishes(dishIds: List<String>): Result<List<String>> {
        if (dishIds.isEmpty()) {
            throw ValidationException("At least one dish must be selected")
        }
        return Result.success(dishIds)
    }
}
