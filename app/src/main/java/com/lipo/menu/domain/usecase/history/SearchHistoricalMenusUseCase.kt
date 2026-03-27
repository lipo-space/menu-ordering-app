package com.lipo.menu.domain.usecase.history

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchHistoricalMenusUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(query: String): Flow<List<TodayMenu>> {
        return todayMenuRepository.searchHistoricalMenus(query)
    }
}
