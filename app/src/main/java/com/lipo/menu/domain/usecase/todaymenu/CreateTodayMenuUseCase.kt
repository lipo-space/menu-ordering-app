package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import com.lipo.menu.domain.validator.TodayMenuValidator
import java.time.LocalDate
import javax.inject.Inject

class CreateTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository,
    private val todayMenuValidator: TodayMenuValidator
) {
    suspend operator fun invoke(date: LocalDate, dishIds: List<String>): Result<TodayMenu> {
        return try {
            // Validate inputs - validators throw exceptions on failure
            todayMenuValidator.validateMenuDate(date)
            todayMenuValidator.validateDishSelection(dishIds)

            // Create the menu
            todayMenuRepository.createTodayMenu(date, dishIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
