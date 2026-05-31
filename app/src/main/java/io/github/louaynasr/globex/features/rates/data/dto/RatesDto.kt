package io.github.louaynasr.globex.features.rates.data.dto

import com.google.gson.annotations.SerializedName

data class RateDto(
    @SerializedName("date") val date: String,
    @SerializedName("base") val base: String,
    @SerializedName("quote") val quote: String,
    @SerializedName("rate") val rate: Double,
)
