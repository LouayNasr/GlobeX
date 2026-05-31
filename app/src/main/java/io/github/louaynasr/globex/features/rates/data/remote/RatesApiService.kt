package io.github.louaynasr.globex.features.rates.data.remote

import io.github.louaynasr.globex.features.rates.data.dto.RateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RatesApiService {

    @GET("v2/rates")
    suspend fun getExchangeRatesWithBase(
        @Query("base") baseCurrency: String,
    ): Response<List<RateDto>>

    @GET("v2/rates")
    suspend fun getExchangeRateWithDate(
        @Query("date") date: String,
        @Query("base") base: String,
    ): Response<List<RateDto>>
}
