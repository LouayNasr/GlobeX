package io.github.louaynasr.globex.features.coverter.data.mappers

import io.github.louaynasr.globex.features.coverter.data.dto.ConversionHistoryDto
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate

fun ConversionHistoryDto.toDomain(): List<HistoricalRate> {
    return this.rates.mapNotNull { (date, currencyMap) ->
        val rate = currencyMap.values.firstOrNull()
        if (rate != null) HistoricalRate(date, rate) else null
    }
}