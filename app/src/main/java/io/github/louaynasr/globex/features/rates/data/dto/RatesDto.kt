package io.github.louaynasr.globex.features.rates.data.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRatesDto(
    @SerializedName("amount") val amount: Double,
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, Double>
)
