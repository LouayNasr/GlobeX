package io.github.louaynasr.globex.features.coverter.data.dto

import com.google.gson.annotations.SerializedName

data class ConversionHistoryDto(
    @SerializedName("amount") val amount: Double,
    @SerializedName("base") val base: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("rates") val rates: Map<String, Map<String, Double>>,
)
