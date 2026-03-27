package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayMenuByIdUseCase @Inject constructor(
    private val todayMenuRepository: TodayMenuRepository
) {
    operator fun invoke(id: String): Flow<TodayMenu?> {
        return todayMenuRepository.getTodayMenuById(id)
    }
}
