package io.github.louaynasr.globex.features.rates.presentation

import io.github.louaynasr.globex.core.presentation.UiText
import io.github.louaynasr.globex.features.rates.domain.model.Rate

data class HomeScreenState(
    val base: String = "EUR",
    val baseName: String = "",
    val baseFlagUrl: String = "",
    val ratesList: List<Rate> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)
