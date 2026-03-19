package io.github.louaynasr.globex.features.rates.data.repository

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.domain.safeApiCall
import io.github.louaynasr.globex.features.rates.data.mappers.toDomain
import io.github.louaynasr.globex.features.rates.data.remote.CurrencyApiService
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyApi: CurrencyApiService
) : CurrencyRepository {
    override suspend fun getCurrencies(): NetworkResult<List<Currency>> {
        return when (val response = safeApiCall {
            currencyApi.getCurrencies()
        }) {
            is NetworkResult.Success -> NetworkResult.Success(response.data.toDomain())
            is NetworkResult.Error -> response.also { println("response: $response") }
        }
    }
}