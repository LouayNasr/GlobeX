package io.github.louaynasr.globex.features.rates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.UiText
import io.github.louaynasr.globex.core.presentation.toUiText
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCurrenciesState(
    val currencies: List<Currency> = emptyList(),
    val visibleCurrencies: Set<String> = emptySet(),
    val initialVisibleCurrencies: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val filteredCurrencies: List<Currency>
        get() = if (searchQuery.isBlank()) currencies else currencies.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(
                searchQuery,
                ignoreCase = true
            )
        }

    val hasChanges: Boolean
        get() = visibleCurrencies != initialVisibleCurrencies

    val isValid: Boolean
        get() = visibleCurrencies.size >= 2

    val isAllSelected: Boolean
        get() = currencies.isNotEmpty() && visibleCurrencies.size == currencies.size
}

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val prefsRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ManageCurrenciesState())
    val state: StateFlow<ManageCurrenciesState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currenciesResult = currencyRepository.getCurrencies()
            val initialVisible = prefsRepository.visibleCurrenciesFlow.first()

            when (currenciesResult) {
                is NetworkResult.Success -> {
                    // If initialVisible is empty, it means all are visible by default
                    val actualVisible = if (initialVisible.isEmpty()) {
                        currenciesResult.data.map { it.code }.toSet()
                    } else {
                        initialVisible
                    }

                    _state.update {
                        it.copy(
                            currencies = currenciesResult.data,
                            visibleCurrencies = actualVisible,
                            initialVisibleCurrencies = actualVisible,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = currenciesResult.error.toUiText()
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleCurrency(code: String) {
        _state.update { currentState ->
            val newVisible = if (currentState.visibleCurrencies.contains(code)) {
                currentState.visibleCurrencies - code
            } else {
                currentState.visibleCurrencies + code
            }
            currentState.copy(visibleCurrencies = newVisible)
        }
    }

    fun toggleAllCurrencies() {
        _state.update { currentState ->
            val newVisible = if (currentState.isAllSelected) {
                emptySet()
            } else {
                currentState.currencies.map { it.code }.toSet()
            }
            currentState.copy(visibleCurrencies = newVisible)
        }
    }

    fun resetChanges() {
        _state.update {
            it.copy(
                visibleCurrencies = it.initialVisibleCurrencies,
                searchQuery = ""
            )
        }
    }

    fun saveChanges() {
        if (!_state.value.isValid) return

        viewModelScope.launch {
            prefsRepository.saveVisibleCurrencies(_state.value.visibleCurrencies)
            _state.update { it.copy(initialVisibleCurrencies = it.visibleCurrencies) }
        }
    }
}
