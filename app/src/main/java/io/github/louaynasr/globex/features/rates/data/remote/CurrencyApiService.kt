package io.github.louaynasr.globex.features.rates.data.remote

import io.github.louaynasr.globex.features.rates.data.dto.CurrencyDto
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApiService {

    @GET("v2/currencies")
    suspend fun getCurrencies(): Response<List<CurrencyDto>>
}