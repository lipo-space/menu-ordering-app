package com.lipo.menu.domain.usecase.combination

import com.lipo.menu.data.model.Combination
import com.lipo.menu.domain.repository.CombinationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCombinationsUseCase @Inject constructor(
    private val combinationRepository: CombinationRepository
) {
    operator fun invoke(): Flow<List<Combination>> {
        return combinationRepository.getAllCombinations()
    }
}
