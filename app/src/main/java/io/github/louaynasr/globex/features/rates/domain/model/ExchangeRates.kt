package io.github.louaynasr.globex.features.rates.domain.model

data class ExchangeRates(
    val amount: Double,
    val base: String,
    val baseFlagUrl: String,
    val date: String,
    val name: String,
    val rates: List<Rate>
)

data class Rate(
    val code: String,
    val name: String,
    val rate: Double,
    val flagUrl: String
)
