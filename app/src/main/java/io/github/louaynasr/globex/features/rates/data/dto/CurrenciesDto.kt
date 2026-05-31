package io.github.louaynasr.globex.features.rates.data.dto

import com.google.gson.annotations.SerializedName

data class CurrencyDto(
    @SerializedName("iso_code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String,
)