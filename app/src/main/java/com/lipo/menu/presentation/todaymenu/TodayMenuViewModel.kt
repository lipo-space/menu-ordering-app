package com.lipo.menu.presentation.todaymenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.Combination
import com.lipo.menu.data.model.Dish
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.usecase.combination.GetAllCombinationsUseCase
import com.lipo.menu.domain.usecase.dish.GetAllDishesUseCase
import com.lipo.menu.domain.usecase.todaymenu.AddDishToTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.ClearTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.CreateTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.DeleteTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.GetTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.RemoveDishFromTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.UpdateTodayMenuUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayMenuUiState(
    val todayMenu: TodayMenu? = null,
    val allDishes: List<Dish> = emptyList(),
    val allCombinations: List<Combination> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showUpdateDialog: TodayMenu? = null,
    val showDeleteDialog: TodayMenu? = null
) {
    fun clearError(): TodayMenuUiState = copy(error = null)
    fun hideCreateDialog(): TodayMenuUiState = copy(showCreateDialog = false)
    fun hideUpdateDialog(): TodayMenuUiState = copy(showUpdateDialog = null)
    fun hideDeleteDialog(): TodayMenuUiState = copy(showDeleteDialog = null)
}

@HiltViewModel
class TodayMenuViewModel @Inject constructor(
    private val getTodayMenuUseCase: GetTodayMenuUseCase,
    private val createTodayMenuUseCase: CreateTodayMenuUseCase,
    private val updateTodayMenuUseCase: UpdateTodayMenuUseCase,
    private val deleteTodayMenuUseCase: DeleteTodayMenuUseCase,
    private val addDishToTodayMenuUseCase: AddDishToTodayMenuUseCase,
    private val removeDishFromTodayMenuUseCase: RemoveDishFromTodayMenuUseCase,
    private val clearTodayMenuUseCase: ClearTodayMenuUseCase,
    private val getAllDishesUseCase: GetAllDishesUseCase,
    private val getAllCombinationsUseCase: GetAllCombinationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayMenuUiState())
    val uiState: StateFlow<TodayMenuUiState> = _uiState.asStateFlow()

    init {
        loadTodayMenu()
        loadAllDishes()
        loadAllCombinations()
    }

    private fun loadTodayMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getTodayMenuUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collect { menu ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayMenu = menu
                        )
                    }
                }
        }
    }

    private fun loadAllDishes() {
        viewModelScope.launch {
            getAllDishesUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { dishes ->
                    _uiState.update {
                        it.copy(allDishes = dishes)
                    }
                }
        }
    }

    private fun loadAllCombinations() {
        viewModelScope.launch {
            getAllCombinationsUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(error = error.message)
                    }
                }
                .collect { combinations ->
                    _uiState.update {
                        it.copy(allCombinations = combinations)
                    }
                }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.hideCreateDialog() }
    }

    fun showUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = it.todayMenu) }
    }

    fun hideUpdateDialog() {
        _uiState.update { it.hideUpdateDialog() }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = it.todayMenu) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.hideDeleteDialog() }
    }

    fun createTodayMenu(date: LocalDate, dishIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = createTodayMenuUseCase(date, dishIds)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showCreateDialog = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun updateTodayMenu(id: String, date: LocalDate, dishIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = updateTodayMenuUseCase(id, date, dishIds)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showUpdateDialog = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun deleteTodayMenu(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = deleteTodayMenuUseCase(id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showDeleteDialog = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun addDishToTodayMenu(todayMenuId: String, dishId: String, order: Int) {
        viewModelScope.launch {
            val result = addDishToTodayMenuUseCase(todayMenuId, dishId, order)

            result.fold(
                onSuccess = {
                    // Dish added successfully, UI will update automatically via Flow
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message)
                    }
                }
            )
        }
    }

    fun removeDishFromTodayMenu(todayMenuId: String, dishId: String) {
        viewModelScope.launch {
            val result = removeDishFromTodayMenuUseCase(todayMenuId, dishId)

            result.fold(
                onSuccess = {
                    // Dish removed successfully, UI will update automatically via Flow
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message)
                    }
                }
            )
        }
    }

    fun clearTodayMenu(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = clearTodayMenuUseCase(date)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.clearError() }
    }
}
