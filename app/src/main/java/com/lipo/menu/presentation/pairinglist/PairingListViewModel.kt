package com.lipo.menu.presentation.pairinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.Combination
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.usecase.combination.CreateCombinationUseCase
import com.lipo.menu.domain.usecase.combination.DeleteCombinationUseCase
import com.lipo.menu.domain.usecase.combination.GetAllCombinationsUseCase
import com.lipo.menu.domain.usecase.combination.SearchCombinationsUseCase
import com.lipo.menu.domain.usecase.combination.UpdateCombinationUseCase
import com.lipo.menu.domain.usecase.dish.GetAllDishesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingListUiState(
    val combinations: List<Combination> = emptyList(),
    val availableDishes: List<Dish> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Combination? = null,
    val showDeleteDialog: Combination? = null
) {
    fun clearError(): PairingListUiState = copy(error = null)
    fun hideCreateDialog(): PairingListUiState = copy(showCreateDialog = false)
    fun hideEditDialog(): PairingListUiState = copy(showEditDialog = null)
    fun hideDeleteDialog(): PairingListUiState = copy(showDeleteDialog = null)
}

@HiltViewModel
class PairingListViewModel @Inject constructor(
    private val getAllCombinationsUseCase: GetAllCombinationsUseCase,
    private val searchCombinationsUseCase: SearchCombinationsUseCase,
    private val createCombinationUseCase: CreateCombinationUseCase,
    private val updateCombinationUseCase: UpdateCombinationUseCase,
    private val deleteCombinationUseCase: DeleteCombinationUseCase,
    private val getAllDishesUseCase: GetAllDishesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingListUiState())
    val uiState: StateFlow<PairingListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        loadCombinations()
        loadAvailableDishes()
    }

    private fun loadCombinations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        getAllCombinationsUseCase()
                    } else {
                        searchCombinationsUseCase(query)
                    }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collect { combinations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            combinations = combinations
                        )
                    }
                }
        }
    }

    private fun loadAvailableDishes() {
        viewModelScope.launch {
            getAllDishesUseCase()
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { dishes ->
                    // Filter out soft-deleted dishes
                    val availableDishes = dishes.filter { !it.isDeleted }
                    _uiState.update { it.copy(availableDishes = availableDishes) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.hideCreateDialog() }
    }

    fun showEditDialog(combination: Combination) {
        _uiState.update { it.copy(showEditDialog = combination) }
    }

    fun hideEditDialog() {
        _uiState.update { it.hideEditDialog() }
    }

    fun showDeleteDialog(combination: Combination) {
        _uiState.update { it.copy(showDeleteDialog = combination) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.hideDeleteDialog() }
    }

    fun createCombination(name: String, description: String?, dishIds: List<String>) {
        viewModelScope.launch {
            val result = createCombinationUseCase(name, description, dishIds)
            result.fold(
                onSuccess = {
                    hideCreateDialog()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun updateCombination(id: String, name: String, description: String?) {
        viewModelScope.launch {
            val result = updateCombinationUseCase(id, name, description)
            result.fold(
                onSuccess = {
                    hideEditDialog()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun deleteCombination(id: String) {
        viewModelScope.launch {
            val result = deleteCombinationUseCase(id)
            result.fold(
                onSuccess = {
                    hideDeleteDialog()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.clearError() }
    }
}
