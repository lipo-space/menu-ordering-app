package com.lipo.menu.domain.usecase.history

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetMenusByDateRangeUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<TodayMenu>> {
        return todayMenuRepository.getMenusByDateRange(startDate, endDate)
    }
}
