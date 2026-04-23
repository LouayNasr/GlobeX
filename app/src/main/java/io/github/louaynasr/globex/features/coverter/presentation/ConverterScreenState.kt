package io.github.louaynasr.globex.features.coverter.presentation

import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate
import io.github.louaynasr.globex.features.coverter.presentation.components.TimeRange


data class ConverterScreenState(
    val firstCurrency: ConverterRate = ConverterRate(
        code = "USD",
        name = "",
        rate = 1.0,
        flagUrl = ""
    ),
    val secondCurrency: ConverterRate = ConverterRate(
        code = "EUR",
        name = "",
        rate = 0.0,
        flagUrl = ""
    ),
    val ratesList: List<ConverterRate> = emptyList(),
    val firstAmount: String = "1.0",
    val secondAmount: String = "1.0",
    val selectedDuration: TimeRange = TimeRange.ONE_MONTH,
    val canvasValues: List<HistoricalRate> = emptyList(),
    val showDialog: Boolean = false,
)


sealed class ConverterActions {
    object FirstItemClicked : ConverterActions()
    object SecondItemClicked : ConverterActions()
    object DialogDismiss : ConverterActions()
    data class FirstItemChanged(val code: String) : ConverterActions()
    data class SecondItemChanged(val code: String) : ConverterActions()
    data class FirstAmountChanged(val amount: String) : ConverterActions()
    data class SecondAmountChanged(val amount: String) : ConverterActions()
    object OnSwapCurrencies : ConverterActions()
    data class DurationChanged(val code: TimeRange) : ConverterActions()
}