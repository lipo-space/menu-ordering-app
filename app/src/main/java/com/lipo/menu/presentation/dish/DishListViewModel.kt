package com.lipo.menu.presentation.dish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.usecase.dish.AddDishUseCase
import com.lipo.menu.domain.usecase.dish.DeleteDishUseCase
import com.lipo.menu.domain.usecase.dish.GetAllDishesUseCase
import com.lipo.menu.domain.usecase.dish.SearchDishesUseCase
import com.lipo.menu.domain.usecase.dish.UpdateDishUseCase
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

data class DishListUiState(
    val dishes: List<Dish> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Dish? = null,
    val showDeleteDialog: Dish? = null
) {
    fun clearError(): DishListUiState = copy(error = null)
    fun hideAddDialog(): DishListUiState = copy(showAddDialog = false)
    fun hideEditDialog(): DishListUiState = copy(showEditDialog = null)
    fun hideDeleteDialog(): DishListUiState = copy(showDeleteDialog = null)
}

@HiltViewModel
class DishListViewModel @Inject constructor(
    private val getAllDishesUseCase: GetAllDishesUseCase,
    private val searchDishesUseCase: SearchDishesUseCase,
    private val addDishUseCase: AddDishUseCase,
    private val updateDishUseCase: UpdateDishUseCase,
    private val deleteDishUseCase: DeleteDishUseCase,
    private val dishRepository: com.lipo.menu.data.repository.DishRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DishListUiState())
    val uiState: StateFlow<DishListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        Log.d("DishListViewModel", "=== ViewModel init started ===")
        // 先从云端同步数据，然后加载本地数据
        viewModelScope.launch {
            try {
                Log.d("DishListViewModel", "Calling syncFromCloud()...")
                dishRepository.syncFromCloud()
                Log.d("DishListViewModel", "syncFromCloud() completed successfully")
            } catch (e: Exception) {
                // 同步失败不影响本地数据显示
                Log.e("DishListViewModel", "=== FAILED to sync from cloud ===", e)
                Log.e("DishListViewModel", "Error type: ${e::class.simpleName}")
                Log.e("DishListViewModel", "Error message: ${e.message}")
                e.printStackTrace()
            }
        }
        loadDishes()
    }

    private fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        getAllDishesUseCase()
                    } else {
                        searchDishesUseCase(query)
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
                .collect { dishes ->
                    _uiState.update {
                        it.copy(
                            dishes = dishes,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.hideAddDialog() }
    }

    fun showEditDialog(dish: Dish) {
        _uiState.update { it.copy(showEditDialog = dish) }
    }

    fun hideEditDialog() {
        _uiState.update { it.hideEditDialog() }
    }

    fun showDeleteDialog(dish: Dish) {
        _uiState.update { it.copy(showDeleteDialog = dish) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.hideDeleteDialog() }
    }

    fun addDish(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            addDishUseCase(name, description)
                .onSuccess {
                    _uiState.update {
                        it.hideAddDialog().copy(isLoading = false, error = null)
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

    fun updateDish(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            updateDishUseCase(id, name, description)
                .onSuccess {
                    _uiState.update {
                        it.hideEditDialog().copy(isLoading = false, error = null)
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
            Log.d("DishListViewModel", "=== User triggered delete dish ===")
            Log.d("DishListViewModel", "Dish ID: $id")

            _uiState.update { it.copy(isLoading = true) }

            deleteDishUseCase(id)
                .onSuccess {
                    Log.d("DishListViewModel", "✓ Delete dish succeeded")
                    _uiState.update {
                        it.hideDeleteDialog().copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    Log.e("DishListViewModel", "✗ Delete dish failed: ${error.message}", error)
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
