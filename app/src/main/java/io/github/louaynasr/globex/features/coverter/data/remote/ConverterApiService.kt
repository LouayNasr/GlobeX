package io.github.louaynasr.globex.features.coverter.data.remote

import io.github.louaynasr.globex.features.rates.data.dto.RateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ConverterApiService {

    @GET("v2/rates")
    suspend fun getConversionHistory(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("base") base: String,
        @Query("quotes") quote: String,
    ): Response<List<RateDto>>
}