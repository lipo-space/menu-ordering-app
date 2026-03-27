package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.data.model.Combination
import com.lipo.menu.domain.repository.CombinationRepository
import com.lipo.menu.domain.validator.CombinationValidator
import javax.inject.Inject

class CreateCombinationUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository,
    private val combinationValidator: CombinationValidator
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        dishIds: List<String>
    ): Result<Combination> {
        return try {
            // Validate inputs
            combinationValidator.validateName(name)
            combinationValidator.validateDescription(description)
            combinationValidator.validateDishes(dishIds)

            // Create combination
            combinationRepository.createCombination(name, description, dishIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
