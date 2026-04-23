package io.github.louaynasr.globex.features.coverter.data.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.domain.safeApiCall
import io.github.louaynasr.globex.features.coverter.data.mappers.toDomain
import io.github.louaynasr.globex.features.coverter.data.remote.ConverterApiService
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate
import io.github.louaynasr.globex.features.coverter.domain.repository.ConverterRepository
import javax.inject.Inject

class ConverterRepositoryImpl @Inject constructor(
    private val converterApi: ConverterApiService
) : ConverterRepository {
    override suspend fun getConversionHistory(
        dateRange: String,
        fromCurrency: String,
        toCurrency: String
    ): NetworkResult<List<HistoricalRate>> {
        return when (val response = safeApiCall {
            converterApi.getConversionHistory(dateRange, fromCurrency, toCurrency)
        }) {
            is NetworkResult.Success -> NetworkResult.Success(response.data.toDomain())
            is NetworkResult.Error -> response.also { println("response: $response") }
        }
    }
}