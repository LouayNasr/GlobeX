package io.github.louaynasr.globex.features.coverter.domain.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate

interface ConverterRepository {
    suspend fun getConversionHistory(
        from: String,
        to: String,
        baseCurrency: String,
        quoteCurrency: String
    ): NetworkResult<List<HistoricalRate>>
}
