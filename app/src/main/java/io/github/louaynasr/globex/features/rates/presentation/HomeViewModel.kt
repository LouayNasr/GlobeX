package io.github.louaynasr.globex.features.rates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.toUiText
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.usecases.GetRatesWithCurrencyUseCase
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    private var fullRatesList: List<Rate> = emptyList()
    private var lastBaseCurrency: String? = null
    private var lastVisibleCurrencies: Set<String>? = null

    init {
        observeCurrencyPreference()
    }

    private fun observeCurrencyPreference() {
        viewModelScope.launch {
            combine(
                prefsRepository.baseCurrencyFlow,
                prefsRepository.visibleCurrenciesFlow
            ) { base, visible ->
                base to visible
            }.collectLatest { (base, visible) ->
                val baseChanged = base != lastBaseCurrency
                val isAddition =
                    visible.isNotEmpty() && visible.any { code -> fullRatesList.none { it.code == code } }
                val wasAllAndNowSpecific =
                    lastVisibleCurrencies?.isEmpty() == true && visible.isNotEmpty()
                val wasSpecificAndNowAll =
                    lastVisibleCurrencies?.isNotEmpty() == true && visible.isEmpty()

                _currenciesDialogState.update {
                    it.copy(
                        selectedBaseCurrency = base
                    )
                }

                if (baseChanged || isAddition || wasSpecificAndNowAll || wasAllAndNowSpecific || fullRatesList.isEmpty()) {
                    fetchRatesInternal(base, visible)
                } else {
                    _homeScreenState.update {
                        it.copy(
                            base = base,
                            ratesList = if (visible.isEmpty()) fullRatesList else fullRatesList.filter { rate ->
                                visible.contains(rate.code)
                            }
                        )
                    }
                }
                lastBaseCurrency = base
                lastVisibleCurrencies = visible
            }
        }
    }

    fun fetchRates() {
        viewModelScope.launch {
            val visible = prefsRepository.visibleCurrenciesFlow.first()
            fetchRatesInternal(_homeScreenState.value.base, visible)
        }
    }

    private suspend fun fetchRatesInternal(currentBase: String, visibleCurrencies: Set<String>) {
        val now = LocalDate.now()
        // in a real world scenario daysToSubtract should equal 1, so 3 is just for ui/ux purpose to show a rate difference
        val lastDate = now.minusDays(3).toString()

        val quotes = if (visibleCurrencies.isNotEmpty()) {
            visibleCurrencies.joinToString(",")
        } else null

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

        when (val result = ratesWithCurrencyUseCase.invoke(lastDate, currentBase, quotes)) {
            is NetworkResult.Success -> {
                fullRatesList = result.data.rates
                _homeScreenState.update {
                    it.copy(
                        base = currentBase,
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