package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.domain.repository.CombinationRepository
import javax.inject.Inject

class DeleteCombinationUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return combinationRepository.deleteCombination(id)
    }
}
