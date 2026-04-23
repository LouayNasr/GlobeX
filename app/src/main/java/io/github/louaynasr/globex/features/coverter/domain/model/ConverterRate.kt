package io.github.louaynasr.globex.features.coverter.domain.model

data class ConverterExchangeRates(
    val amount: Double,
    val base: String,
    val baseFlagUrl: String,
    val date: String,
    val name: String,
    val rates: List<ConverterRate>
)

data class ConverterRate(
    val code: String,
    val name: String,
    val rate: Double,
    val flagUrl: String
)
