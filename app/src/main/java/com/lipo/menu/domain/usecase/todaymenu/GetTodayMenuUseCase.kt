package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<TodayMenu?> {
        return todayMenuRepository.getTodayMenu(date)
    }
}
