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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCurrenciesState(
    val currencies: List<Currency> = emptyList(),
    val visibleCurrencies: Set<String> = emptySet(),
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
}

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val prefsRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ManageCurrenciesState())
    val state: StateFlow<ManageCurrenciesState> = _state.asStateFlow()

    init {
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currenciesResult = currencyRepository.getCurrencies()

            combine(
                prefsRepository.visibleCurrenciesFlow,
                MutableStateFlow(currenciesResult)
            ) { visible, result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                currencies = result.data,
                                visibleCurrencies = visible,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.toUiText()
                            )
                        }
                    }
                }
            }.first()
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleCurrency(code: String) {
        viewModelScope.launch {
            val currentVisible = prefsRepository.visibleCurrenciesFlow.first()
            val newVisible = if (currentVisible.isEmpty()) {
                // If empty, it means all are visible. Populate with all except the toggled one (effectively deselecting it)
                // Actually, if it's empty, and we toggle one, we probably want to select ALL except that one.
                // Or better: if it's empty, we fetch all codes and then remove the toggled one.
                _state.value.currencies.map { it.code }.toSet() - code
            } else if (currentVisible.contains(code)) {
                currentVisible - code
            } else {
                currentVisible + code
            }
            prefsRepository.saveVisibleCurrencies(newVisible)
            _state.update { it.copy(visibleCurrencies = newVisible) }
        }
    }
}
