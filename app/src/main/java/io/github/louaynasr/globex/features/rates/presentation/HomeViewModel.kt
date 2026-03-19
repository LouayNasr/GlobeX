package io.github.louaynasr.globex.features.rates.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.toUiText
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.usecases.GetRatesWithCurrencyUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefsRepository: PreferencesRepository,
    private val ratesWithCurrencyUseCase: GetRatesWithCurrencyUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    var homeScreenState by mutableStateOf(HomeScreenState())
        private set

    var currenciesDialogState by mutableStateOf(CurrenciesDialogState())
        private set

    init {
        observeCurrencyPreference()
    }

    fun observeCurrencyPreference() {
        viewModelScope.launch {
            prefsRepository.baseCurrencyFlow.collect { savedCurrency ->
                homeScreenState = homeScreenState.copy(
                    base = savedCurrency
                )
                currenciesDialogState = currenciesDialogState.copy(
                    selectedBaseCurrency = savedCurrency
                )
                fetchRates()
            }
        }
    }

    fun fetchRates() {
        viewModelScope.launch {
            val currentBase = homeScreenState.base

            when (val result = ratesWithCurrencyUseCase.invoke(currentBase)) {
                is NetworkResult.Success -> {
                    homeScreenState = homeScreenState.copy(
                        baseName = result.data.name,
                        baseFlagUrl = result.data.baseFlagUrl,
                        ratesList = result.data.rates,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is NetworkResult.Error -> {
                    homeScreenState = homeScreenState.copy(
                        ratesList = emptyList(),
                        isLoading = false,
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
                    currenciesDialogState = currenciesDialogState.copy(
                        currencyList = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is NetworkResult.Error -> {
                    currenciesDialogState = currenciesDialogState.copy(
                        currencyList = emptyList(),
                        isLoading = false,
                        errorMessage = result.error.toUiText()
                    )
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