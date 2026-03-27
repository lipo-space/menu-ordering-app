package com.lipo.menu.presentation.pairinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.Combination
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.usecase.combination.AddDishToCombinationUseCase
import com.lipo.menu.domain.usecase.combination.GetCombinationByIdUseCase
import com.lipo.menu.domain.usecase.combination.RemoveDishFromCombinationUseCase
import com.lipo.menu.domain.usecase.combination.UpdateCombinationUseCase
import com.lipo.menu.domain.usecase.dish.GetAllDishesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingListDetailUiState(
    val combination: Combination? = null,
    val availableDishes: List<Dish> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditDialog: Boolean = false,
    val showAddDishDialog: Boolean = false,
    val showRemoveDishDialog: Dish? = null
) {
    fun clearError(): PairingListDetailUiState = copy(error = null)
}

@HiltViewModel
class PairingListDetailViewModel @Inject constructor(
    private val getCombinationByIdUseCase: GetCombinationByIdUseCase,
    private val updateCombinationUseCase: UpdateCombinationUseCase,
    private val addDishToCombinationUseCase: AddDishToCombinationUseCase,
    private val removeDishFromCombinationUseCase: RemoveDishFromCombinationUseCase,
    private val getAllDishesUseCase: GetAllDishesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingListDetailUiState())
    val uiState: StateFlow<PairingListDetailUiState> = _uiState.asStateFlow()

    fun loadCombination(combinationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getCombinationByIdUseCase(combinationId)
                .collect { combination ->
                    _uiState.update {
                        it.copy(
                            combination = combination,
                            isLoading = false,
                            error = if (combination == null) "Combination not found" else null
                        )
                    }
                }
        }
    }

    fun showEditDialog() {
        _uiState.update { it.copy(showEditDialog = true) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false) }
    }

    fun showAddDishDialog() {
        viewModelScope.launch {
            // Load all available dishes (excluding soft-deleted ones)
            val allDishes = getAllDishesUseCase().first()
            val currentDishIds = _uiState.value.combination?.dishes?.map { it.id } ?: emptyList()
            val availableDishes = allDishes.filter { dish ->
                dish.id !in currentDishIds && !dish.isDeleted
            }

            _uiState.update {
                it.copy(
                    availableDishes = availableDishes,
                    showAddDishDialog = true
                )
            }
        }
    }

    fun hideAddDishDialog() {
        _uiState.update {
            it.copy(
                showAddDishDialog = false,
                availableDishes = emptyList()
            )
        }
    }

    fun showRemoveDishDialog(dish: Dish) {
        _uiState.update { it.copy(showRemoveDishDialog = dish) }
    }

    fun hideRemoveDishDialog() {
        _uiState.update { it.copy(showRemoveDishDialog = null) }
    }

    fun updateCombination(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            updateCombinationUseCase(id, name, description)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showEditDialog = false,
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

    fun addDishToCombination(combinationId: String, dishId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            addDishToCombinationUseCase(combinationId, dishId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showAddDishDialog = false,
                            availableDishes = emptyList(),
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

    fun removeDishFromCombination(combinationId: String, dishId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            removeDishFromCombinationUseCase(combinationId, dishId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showRemoveDishDialog = null,
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
        _uiState.update { it.clearError() }
    }
}
