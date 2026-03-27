package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject

@OptIn(FlowPreview::class)
class SearchDishesUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    operator fun invoke(query: String): Flow<List<Dish>> {
        return dishRepository.searchDishes(query.trim())
            .debounce(300) // 300ms debounce for performance (SC-003)
    }
}
