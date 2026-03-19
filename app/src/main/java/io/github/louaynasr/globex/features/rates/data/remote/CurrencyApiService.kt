package io.github.louaynasr.globex.features.rates.data.remote

import io.github.louaynasr.globex.features.rates.data.dto.CurrenciesDto
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApiService {

    @GET("/v1/currencies")
    suspend fun getCurrencies(): Response<CurrenciesDto>
}