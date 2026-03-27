package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.data.model.Combination
import com.lipo.menu.domain.repository.CombinationRepository
import com.lipo.menu.domain.validator.CombinationValidator
import javax.inject.Inject

class UpdateCombinationUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository,
    private val combinationValidator: CombinationValidator
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        description: String?
    ): Result<Combination> {
        return try {
            // Validate inputs
            combinationValidator.validateName(name)
            combinationValidator.validateDescription(description)

            // Update combination
            combinationRepository.updateCombination(id, name, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
