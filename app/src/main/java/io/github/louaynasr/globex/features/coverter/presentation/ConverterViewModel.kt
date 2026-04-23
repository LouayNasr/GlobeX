package io.github.louaynasr.globex.features.coverter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.toUiText
import io.github.louaynasr.globex.core.util.validateCurrencyInput
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate
import io.github.louaynasr.globex.features.coverter.domain.repository.ConverterRepository
import io.github.louaynasr.globex.features.coverter.domain.usecases.GetConverterRatesUseCase
import io.github.louaynasr.globex.features.coverter.presentation.components.TimeRange
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val prefsRepository: PreferencesRepository,
    private val converterRepository: ConverterRepository,
    private val converterRatesUseCase: GetConverterRatesUseCase,
    private val currenciesRepository: CurrencyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterScreenState())
    val uiState: StateFlow<ConverterScreenState> = _uiState.asStateFlow()

    private val _dialogUiState = MutableStateFlow(CurrenciesDialogState())
    val dialogUiState: StateFlow<CurrenciesDialogState> = _dialogUiState.asStateFlow()

    fun onAction(action: ConverterActions) {
        when (action) {
            ConverterActions.FirstItemClicked -> onTopItemClicked()
            ConverterActions.SecondItemClicked -> onBottomItemClicked()
            ConverterActions.DialogDismiss -> onDialogDismiss()
            is ConverterActions.FirstItemChanged -> onTopCurrencyChange(action.code)
            is ConverterActions.SecondItemChanged -> onBottomCurrencyChange(action.code)
            is ConverterActions.FirstAmountChanged -> onTopAmountChange(action.amount)
            is ConverterActions.SecondAmountChanged -> onBottomAmountChange(action.amount)
            ConverterActions.OnSwapCurrencies -> onSwapCurrencies()
            is ConverterActions.DurationChanged -> onDurationSelected(action.code)
        }
    }

    init {
        observeCurrencies()
    }

    private fun observeCurrencies() {
        viewModelScope.launch {
            combine(
                prefsRepository.converterFirstCurrencyFlow,
                prefsRepository.converterSecondCurrencyFlow
            ) { first, second -> first to second }
                .collect { (firstCode, secondCode) ->
                    val currentState = _uiState.value

                    if (firstCode != currentState.firstCurrency.code || currentState.ratesList.isEmpty()) {
                        fetchRates(firstCode, secondCode)
                    } else if (secondCode != currentState.secondCurrency.code) {
                        updateSecondCurrency(secondCode)
                    }
                }
        }
    }

    private fun fetchRates(baseCode: String, targetCode: String) {
        viewModelScope.launch {
            when (val result = converterRatesUseCase(baseCode)) {
                is NetworkResult.Success -> {
                    val rates = result.data.rates
                    val targetRate = rates.find { it.code == targetCode }
                        ?: ConverterRate(targetCode, "", 0.0, "")

                    _uiState.update {
                        it.copy(
                            firstCurrency = it.firstCurrency.copy(
                                code = baseCode,
                                name = result.data.name,
                                flagUrl = result.data.baseFlagUrl,
                                rate = 1.0
                            ),
                            secondCurrency = targetRate,
                            ratesList = rates
                        )
                    }
                    recalculateAmounts(fromFirst = true)
                    fetchHistory()
                }

                is NetworkResult.Error -> {
                    // TODO: Implement error handling (e.g., snackbar)
                }
            }
        }
    }

    fun fetchCurrencies() {
        viewModelScope.launch {
            when (val result = currenciesRepository.getCurrencies()) {
                is NetworkResult.Success -> {
                    _dialogUiState.update {
                        it.copy(
                            currencyList = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _dialogUiState.update {
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

    private fun updateSecondCurrency(targetCode: String) {
        val targetRate = _uiState.value.ratesList.find { it.code == targetCode }
            ?: ConverterRate(targetCode, "", 0.0, "")

        _uiState.update {
            it.copy(
                secondCurrency = targetRate
            )
        }
        recalculateAmounts(fromFirst = true)
        fetchHistory()
    }

    private fun recalculateAmounts(fromFirst: Boolean) {
        _uiState.update { state ->
            val rate = state.secondCurrency.rate
            if (fromFirst) {
                val amount = state.firstAmount.toDoubleOrNull() ?: 0.0
                val calculated = "%.2f".format(amount * rate)
                state.copy(secondAmount = calculated)
            } else {
                val amount = state.secondAmount.toDoubleOrNull() ?: 0.0
                val calculated = if (rate != 0.0) "%.2f".format(amount / rate) else "0.00"
                state.copy(firstAmount = calculated)
            }
        }
    }

    private fun fetchHistory() {
        val state = _uiState.value
        val fromCurrency = state.firstCurrency.code
        val toCurrency = state.secondCurrency.code
        val duration = state.selectedDuration

        val daysToSubtract: Long = when (duration) {
            TimeRange.ONE_MONTH -> 30L
            TimeRange.THREE_MONTHS -> 90L
            else -> 365L
        }

        val now = LocalDate.now()
        val pastDate = "${now.minusDays(daysToSubtract)}.."

        viewModelScope.launch {
            when (val result =
                converterRepository.getConversionHistory(pastDate, fromCurrency, toCurrency)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(canvasValues = result.data) }
                }

                is NetworkResult.Error -> {
                    // TODO: Implement error handling
                }
            }
        }
    }

    private fun onTopItemClicked() {
        _dialogUiState.update {
            it.copy(
                isLoading = true,
                selectedBaseCurrency = _uiState.value.firstCurrency.code
            )
        }
        _uiState.update {
            it.copy(
                showDialog = true
            )
        }
        fetchCurrencies()
    }

    private fun onBottomItemClicked() {

        _dialogUiState.update {
            it.copy(
                isLoading = true,
                selectedBaseCurrency = _uiState.value.secondCurrency.code
            )
        }
        _uiState.update {
            it.copy(
                showDialog = true
            )
        }
        fetchCurrencies()
    }

    private fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showDialog = false
            )
        }
    }

    private fun onTopCurrencyChange(code: String) {
        viewModelScope.launch {
            prefsRepository.saveConverterFirstCurrency(code)
        }
        _uiState.update {
            it.copy(
                showDialog = false
            )
        }
    }

    private fun onBottomCurrencyChange(code: String) {
        viewModelScope.launch {
            prefsRepository.saveConverterSecondCurrency(code)
        }
        _uiState.update {
            it.copy(
                showDialog = false
            )
        }
    }

    private fun onTopAmountChange(amount: String) {
        val validated = validateCurrencyInput(amount, _uiState.value.firstAmount)
        _uiState.update { it.copy(firstAmount = validated) }
        recalculateAmounts(fromFirst = true)
    }

    private fun onBottomAmountChange(amount: String) {
        val validated = validateCurrencyInput(amount, _uiState.value.secondAmount)
        _uiState.update { it.copy(secondAmount = validated) }
        recalculateAmounts(fromFirst = false)
    }

    private fun onSwapCurrencies() {

        val currentState = _uiState.value
        val firstCode = currentState.firstCurrency.code
        val secondCode = currentState.secondCurrency.code

        viewModelScope.launch {
            prefsRepository.saveConverterSecondCurrency(firstCode)
            prefsRepository.saveConverterFirstCurrency(secondCode)
        }
    }

    private fun onDurationSelected(duration: TimeRange) {
        _uiState.update { it.copy(selectedDuration = duration) }
        fetchHistory()
    }

}