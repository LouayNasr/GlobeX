package io.github.louaynasr.globex.features.rates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.toUiText
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.usecases.GetRatesWithCurrencyUseCase
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefsRepository: PreferencesRepository,
    private val ratesWithCurrencyUseCase: GetRatesWithCurrencyUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState.asStateFlow()

    private val _currenciesDialogState = MutableStateFlow(CurrenciesDialogState())
    val currenciesDialogState: StateFlow<CurrenciesDialogState> =
        _currenciesDialogState.asStateFlow()

    init {
        observeCurrencyPreference()
    }

    fun observeCurrencyPreference() {
        viewModelScope.launch {
            prefsRepository.baseCurrencyFlow.collectLatest { savedCurrency ->
                _homeScreenState.update {
                    it.copy(
                        base = savedCurrency
                    )
                }
                _currenciesDialogState.update {
                    it.copy(
                        selectedBaseCurrency = savedCurrency
                    )
                }
                fetchRatesInternal(savedCurrency)
            }
        }
    }

    fun fetchRates() {
        viewModelScope.launch {
            fetchRatesInternal(_homeScreenState.value.base)
        }
    }

    private suspend fun fetchRatesInternal(currentBase: String) {
        val now = LocalDate.now()
        // in a real world scenario daysToSubtract should equal 1, so 3 is just for ui/ux purpose to show a rate difference
        val lastDate = now.minusDays(3).toString()

        // Determine if this is a background refresh or initial load
        val isManualRefresh = _homeScreenState.value.ratesList.isNotEmpty()

        if (isManualRefresh) {
            _homeScreenState.update {
                it.copy(
                    isRefreshing = true
                )
            }
        } else {
            _homeScreenState.update {
                it.copy(
                    isLoading = true
                )
            }
        }

        when (val result = ratesWithCurrencyUseCase.invoke(lastDate, currentBase)) {
            is NetworkResult.Success -> {
                _homeScreenState.update {
                    it.copy(
                        baseName = result.data.name,
                        baseFlagUrl = result.data.baseFlagUrl,
                        ratesList = result.data.rates,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }

            is NetworkResult.Error -> {
                _homeScreenState.update {
                    it.copy(
                        ratesList = emptyList(),
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = result.error.toUiText()
                    )
                }
            }
        }
    }

    fun fetchCurrencies() {
        viewModelScope.launch {
            when (val result = currencyRepository.getCurrencies()) {
                is NetworkResult.Success -> {
                    _currenciesDialogState.update {
                        it.copy(
                            currencyList = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _currenciesDialogState.update {
                        it.copy(
                            currencyList = emptyList(),
                            isLoading = false,
                            errorMessage = result.error.toUiText()
                        )
                    }
                }
            }
        }
    }

    fun onCurrencyChanged(newCurrency: String) {
        viewModelScope.launch {
            prefsRepository.saveBaseCurrency(newCurrency)
        }
    }
}