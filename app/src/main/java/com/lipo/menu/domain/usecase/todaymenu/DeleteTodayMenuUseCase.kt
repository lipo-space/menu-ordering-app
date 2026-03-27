package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.domain.repository.TodayMenuRepository
import javax.inject.Inject

class DeleteTodayMenuUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return todayMenuRepository.deleteTodayMenu(id)
    }
}
