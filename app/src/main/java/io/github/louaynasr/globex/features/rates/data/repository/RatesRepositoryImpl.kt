package io.github.louaynasr.globex.features.rates.data.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.domain.safeApiCall
import io.github.louaynasr.globex.features.rates.data.mappers.toDomain
import io.github.louaynasr.globex.features.rates.data.remote.RatesApiService
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import javax.inject.Inject

class RatesRepositoryImpl @Inject constructor(
    private val apiService: RatesApiService
) : RatesRepository {

    override suspend fun getExchangeRatesWithBase(baseCurrency: String): NetworkResult<ExchangeRates> {
        return when (val response = safeApiCall {
            apiService.getExchangeRatesWithBase(baseCurrency)
        }) {
            is NetworkResult.Success -> NetworkResult.Success(response.data.toDomain())
            is NetworkResult.Error -> response.also { println("response: $response") }
        }
    }

    override suspend fun getExchangeRatesWithDate(
        date: String,
        baseCurrency: String
    ): NetworkResult<ExchangeRates> {
        return when (val response = safeApiCall {
            apiService.getExchangeRateWithDate(date, baseCurrency)
        }) {
            is NetworkResult.Success -> NetworkResult.Success(response.data.toDomain())
            is NetworkResult.Error -> response
        }
    }
}