package io.github.louaynasr.globex.features.coverter.data.mappers

import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate
import io.github.louaynasr.globex.features.rates.data.dto.RateDto

fun List<RateDto>.toDomain(): List<HistoricalRate> {
    return map { dto ->
        HistoricalRate(
            date = dto.date,
            rate = dto.rate
        )
    }
}