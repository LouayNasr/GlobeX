package io.github.louaynasr.globex.features.rates.domain.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.rates.domain.model.Currency

interface CurrencyRepository {
    suspend fun getCurrencies(): NetworkResult<List<Currency>>
}