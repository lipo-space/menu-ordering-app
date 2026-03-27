package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import javax.inject.Inject

class UpdateDishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    suspend operator fun invoke(id: String, name: String, description: String?): Result<Dish> {
        return dishRepository.updateDish(id, name.trim(), description?.trim())
    }
}
