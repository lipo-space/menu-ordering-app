package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import com.lipo.menu.domain.validator.TodayMenuValidator
import java.time.LocalDate
import javax.inject.Inject

class UpdateTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository,
    private val todayMenuValidator: TodayMenuValidator
) {
    suspend operator fun invoke(id: String, date: LocalDate, dishIds: List<String>): Result<TodayMenu> {
        return try {
            // Validate inputs
            todayMenuValidator.validateMenuDate(date)
            todayMenuValidator.validateDishSelection(dishIds)

            // Update the menu
            todayMenuRepository.updateTodayMenu(id, date, dishIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
