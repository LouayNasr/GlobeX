package io.github.louaynasr.globex.features.coverter.data.remote

import io.github.louaynasr.globex.features.coverter.data.dto.ConversionHistoryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ConverterApiService {

    @GET("v1/{dateRange}")
    suspend fun getConversionHistory(
        @Path("dateRange") dateRange: String,
        @Query("base") fromCurrency: String,
        @Query("symbols") toCurrency: String,
    ): Response<ConversionHistoryDto>
}