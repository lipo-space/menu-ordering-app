package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class ClearTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): Result<Unit> {
        return try {
            val menu = todayMenuRepository.getTodayMenu(date).first()
            if (menu != null) {
                todayMenuRepository.updateTodayMenu(menu.id, date, emptyList())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
