package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.domain.repository.TodayMenuRepository
import javax.inject.Inject

class RemoveDishFromTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    suspend operator fun invoke(todayMenuId: String, dishId: String): Result<Unit> {
        return todayMenuRepository.removeDishFromTodayMenu(todayMenuId, dishId)
    }
}
