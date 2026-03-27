package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDishUsageCountUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(dishId: String): Flow<Int> {
        return todayMenuRepository.getUsageCountForDish(dishId)
    }
}
