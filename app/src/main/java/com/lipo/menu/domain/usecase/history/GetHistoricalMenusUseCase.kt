package com.lipo.menu.domain.usecase.history

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoricalMenusUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(): Flow<List<TodayMenu>> {
        return todayMenuRepository.getHistoricalMenus()
    }
}
