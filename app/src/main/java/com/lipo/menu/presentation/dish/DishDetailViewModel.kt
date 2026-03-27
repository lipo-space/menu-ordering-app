package com.lipo.menu.presentation.dish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.usecase.dish.DeleteDishUseCase
import com.lipo.menu.domain.usecase.dish.GetDishCombinationCountUseCase
import com.lipo.menu.domain.usecase.dish.GetDishUseCase
import com.lipo.menu.domain.usecase.dish.GetDishUsageCountUseCase
import com.lipo.menu.domain.usecase.dish.UpdateDishUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DishDetailUiState(
    val dish: Dish? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditDialog: Dish? = null,
    val showDeleteDialog: Boolean = false,
    val combinationCount: Int = 0,
    val usageCount: Int = 0
)

@HiltViewModel
class DishDetailViewModel @Inject constructor(
    private val getDishUseCase: GetDishUseCase,
    private val updateDishUseCase: UpdateDishUseCase,
    private val deleteDishUseCase: DeleteDishUseCase,
    private val getDishCombinationCountUseCase: GetDishCombinationCountUseCase,
    private val getDishUsageCountUseCase: GetDishUsageCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DishDetailUiState())
    val uiState: StateFlow<DishDetailUiState> = _uiState.asStateFlow()

    fun loadDish(dishId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine dish data with statistics
            combine(
                getDishUseCase(dishId),
                getDishCombinationCountUseCase(dishId),
                getDishUsageCountUseCase(dishId)
            ) { dish, combinationCount, usageCount ->
                Triple(dish, combinationCount, usageCount)
            }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collect { (dish, combinationCount, usageCount) ->
                    _uiState.update {
                        it.copy(
                            dish = dish,
                            isLoading = false,
                            error = if (dish == null) "Dish not found" else null,
                            combinationCount = combinationCount,
                            usageCount = usageCount
                        )
                    }
                }
        }
    }

    fun showEditDialog() {
        _uiState.value.dish?.let { dish ->
            _uiState.update { it.copy(showEditDialog = dish) }
        }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = null) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun updateDish(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            updateDishUseCase(id, name, description)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showEditDialog = null,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun deleteDish(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            deleteDishUseCase(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showDeleteDialog = false,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
