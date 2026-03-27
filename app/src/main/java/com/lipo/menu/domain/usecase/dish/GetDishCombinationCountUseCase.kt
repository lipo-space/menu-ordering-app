package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.domain.repository.CombinationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDishCombinationCountUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository
) {
    operator fun invoke(dishId: String): Flow<Int> {
        return combinationRepository.getCombinationCountForDish(dishId)
    }
}
