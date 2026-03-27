package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.domain.repository.CombinationRepository
import javax.inject.Inject

class RemoveDishFromCombinationUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository
) {
    suspend operator fun invoke(combinationId: String, dishId: String): Result<Unit> {
        return try {
            combinationRepository.removeDishFromCombination(combinationId, dishId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
