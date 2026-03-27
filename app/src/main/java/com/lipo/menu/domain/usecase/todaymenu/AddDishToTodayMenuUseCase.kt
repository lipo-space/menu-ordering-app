package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.domain.repository.TodayMenuRepository
import javax.inject.Inject

class AddDishToTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    suspend operator fun invoke(todayMenuId: String, dishId: String, order: Int = 0): Result<Unit> {
        return todayMenuRepository.addDishToTodayMenu(todayMenuId, dishId, order)
    }
}
