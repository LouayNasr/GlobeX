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
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
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
            prefsRepository.baseCurrencyFlow.collectLatest { savedCurrency ->
                homeScreenState = homeScreenState.copy(
                    base = savedCurrency
                )
                currenciesDialogState = currenciesDialogState.copy(
                    selectedBaseCurrency = savedCurrency
                )
                fetchRatesInternal(savedCurrency)
            }
        }
    }

    fun fetchRates() {
        viewModelScope.launch {
            fetchRatesInternal(homeScreenState.base)
        }
    }

    private suspend fun fetchRatesInternal(currentBase: String) {
        val now = LocalDate.now()
        // in a real world scenario daysToSubtract should equal 1, so 3 is just for ui/ux purpose to show a rate difference
        val lastDate = now.minusDays(3).toString()

        // Determine if this is a background refresh or initial load
        val isManualRefresh = homeScreenState.ratesList.isNotEmpty()

        homeScreenState = if (isManualRefresh) {
            homeScreenState.copy(isRefreshing = true)
        } else {
            homeScreenState.copy(isLoading = true)
        }


        when (val result = ratesWithCurrencyUseCase.invoke(lastDate, currentBase)) {
            is NetworkResult.Success -> {
                homeScreenState = homeScreenState.copy(
                    baseName = result.data.name,
                    baseFlagUrl = result.data.baseFlagUrl,
                    ratesList = result.data.rates,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }

            is NetworkResult.Error -> {
                homeScreenState = homeScreenState.copy(
                    ratesList = emptyList(),
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = result.error.toUiText()
                )
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