package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    operator fun invoke(id: String): Flow<Dish?> {
        return dishRepository.getDishById(id)
    }
}
