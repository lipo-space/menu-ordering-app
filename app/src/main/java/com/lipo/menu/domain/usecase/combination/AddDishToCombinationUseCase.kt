package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.domain.repository.CombinationRepository
import javax.inject.Inject

class AddDishToCombinationUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository
) {
    suspend operator fun invoke(combinationId: String, dishId: String): Result<Unit> {
        return try {
            combinationRepository.addDishToCombination(combinationId, dishId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
