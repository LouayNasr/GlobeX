package io.github.louaynasr.globex.features.coverter.domain.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate

interface ConverterRepository {
    suspend fun getConversionHistory(
        dateRange: String,
        fromCurrency: String,
        toCurrency: String
    ): NetworkResult<List<HistoricalRate>>
}

