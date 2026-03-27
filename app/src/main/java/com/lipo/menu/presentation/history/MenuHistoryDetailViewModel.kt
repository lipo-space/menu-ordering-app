package com.lipo.menu.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.usecase.todaymenu.GetTodayMenuByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuHistoryDetailUiState(
    val menu: TodayMenu? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MenuHistoryDetailViewModel @Inject constructor(
    private val getTodayMenuByIdUseCase: GetTodayMenuByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuHistoryDetailUiState())
    val uiState: StateFlow<MenuHistoryDetailUiState> = _uiState.asStateFlow()

    fun loadMenu(menuId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getTodayMenuByIdUseCase(menuId)
                .collect { menu ->
                    _uiState.update {
                        it.copy(
                            menu = menu,
                            isLoading = false,
                            error = if (menu == null) "Menu not found" else null
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
