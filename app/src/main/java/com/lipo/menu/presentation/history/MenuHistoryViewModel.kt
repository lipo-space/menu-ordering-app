package com.lipo.menu.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.usecase.history.DeleteHistoricalMenuUseCase
import com.lipo.menu.domain.usecase.history.GetHistoricalMenusUseCase
import com.lipo.menu.domain.usecase.history.GetMenusByDateRangeUseCase
import com.lipo.menu.domain.usecase.history.SearchHistoricalMenusUseCase
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
import java.time.LocalDate
import javax.inject.Inject

data class MenuHistoryUiState(
    val historicalMenus: List<TodayMenu> = emptyList(),
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.All,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: TodayMenu? = null
) {
    fun clearError(): MenuHistoryUiState = copy(error = null)
    fun hideDeleteDialog(): MenuHistoryUiState = copy(showDeleteDialog = null)
}

sealed class DateFilter {
    object All : DateFilter()
    object LastWeek : DateFilter()
    object LastMonth : DateFilter()
    object LastThreeMonths : DateFilter()
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : DateFilter()
}

@HiltViewModel
class MenuHistoryViewModel @Inject constructor(
    private val getHistoricalMenusUseCase: GetHistoricalMenusUseCase,
    private val getMenusByDateRangeUseCase: GetMenusByDateRangeUseCase,
    private val searchHistoricalMenusUseCase: SearchHistoricalMenusUseCase,
    private val deleteHistoricalMenuUseCase: DeleteHistoricalMenuUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuHistoryUiState())
    val uiState: StateFlow<MenuHistoryUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val dateFilter = MutableStateFlow<DateFilter>(DateFilter.All)

    init {
        loadHistoricalMenus()
    }

    private fun loadHistoricalMenus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isNotBlank()) {
                        searchHistoricalMenusUseCase(query)
                    } else {
                        getHistoricalMenusUseCase()
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
                .collect { menus ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historicalMenus = menus
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun onDateFilterChanged(filter: DateFilter) {
        _uiState.update { it.copy(dateFilter = filter) }
        dateFilter.value = filter

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = when (filter) {
                is DateFilter.All -> getHistoricalMenusUseCase()
                is DateFilter.LastWeek -> {
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusWeeks(1)
                    getMenusByDateRangeUseCase(startDate, endDate)
                }
                is DateFilter.LastMonth -> {
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusMonths(1)
                    getMenusByDateRangeUseCase(startDate, endDate)
                }
                is DateFilter.LastThreeMonths -> {
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusMonths(3)
                    getMenusByDateRangeUseCase(startDate, endDate)
                }
                is DateFilter.Custom -> {
                    getMenusByDateRangeUseCase(filter.startDate, filter.endDate)
                }
            }

            result
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collect { menus ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historicalMenus = menus
                        )
                    }
                }
        }
    }

    fun showDeleteDialog(menu: TodayMenu) {
        _uiState.update { it.copy(showDeleteDialog = menu) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.hideDeleteDialog() }
    }

    fun deleteHistoricalMenu(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = deleteHistoricalMenuUseCase(id)

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

    fun clearError() {
        _uiState.update { it.clearError() }
    }
}
