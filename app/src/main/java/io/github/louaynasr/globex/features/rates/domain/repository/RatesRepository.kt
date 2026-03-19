package io.github.louaynasr.globex.features.rates.domain.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates

interface RatesRepository {
    suspend fun getExchangeRatesWithBase(baseCurrency: String): NetworkResult<ExchangeRates>
}
