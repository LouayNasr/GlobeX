package io.github.louaynasr.globex.features.rates.data.remote

import io.github.louaynasr.globex.features.rates.data.dto.ExchangeRatesDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RatesApiService {

    @GET("/v1/latest")
    suspend fun getExchangeRatesWithBase(
        @Query("base") baseCurrency: String
    ): Response<ExchangeRatesDto>
}


