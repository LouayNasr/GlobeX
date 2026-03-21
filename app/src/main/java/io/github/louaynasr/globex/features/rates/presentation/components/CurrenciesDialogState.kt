package io.github.louaynasr.globex.features.rates.presentation.components

import io.github.louaynasr.globex.core.presentation.UiText
import io.github.louaynasr.globex.features.rates.domain.model.Currency

data class CurrenciesDialogState(
    val currencyList: List<Currency> = emptyList(),
    val isLoading: Boolean = true,
    val selectedBaseCurrency: String = "",
    val errorMessage: UiText? = null
)