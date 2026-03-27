package com.lipo.menu.domain.validator

class DishValidator {

    fun validateName(name: String): Result<String> {
        if (name.isBlank()) {
            throw ValidationException("Dish name cannot be empty or blank")
        }
        if (name.length > 100) {
            throw ValidationException("Dish name cannot exceed 100 characters")
        }
        return Result.success(name.trim())
    }

    fun validateDescription(description: String?): Result<String?> {
        if (description != null && description.length > 500) {
            throw ValidationException("Description cannot exceed 500 characters")
        }
        return Result.success(description?.trim())
    }
}
