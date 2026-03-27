package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class AddDishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    suspend operator fun invoke(name: String, description: String?): Result<Dish> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Dish name cannot be empty"))
        }

        if (name.length > 100) {
            return Result.failure(IllegalArgumentException("Dish name cannot exceed 100 characters"))
        }

        if (description != null && description.length > 500) {
            return Result.failure(IllegalArgumentException("Description cannot exceed 500 characters"))
        }

        return dishRepository.addDish(name, description)
    }
}
