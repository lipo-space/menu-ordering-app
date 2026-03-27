package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.domain.repository.DishRepository
import javax.inject.Inject

class DeleteDishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return dishRepository.deleteDish(id)
    }
}
