package io.github.louaynasr.globex.features.rates.data.mappers

import io.github.louaynasr.globex.features.rates.data.dto.RateDto
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend

fun List<RateDto>.toDomain(): ExchangeRates {
    val first = firstOrNull()
    return ExchangeRates(
        amount = 1.0,
        base = first?.base ?: "",
        baseFlagUrl = "https://flagcdn.com/80x60/${first?.base?.take(2)?.lowercase() ?: ""}.png",
        date = first?.date ?: "",
        name = "",
        rates = map { dto ->
            Rate(
                code = dto.quote,
                name = "",
                rate = dto.rate,
                flagUrl = "https://flagcdn.com/80x60/${dto.quote.take(2).lowercase()}.png",
                changePercentage = "0.0",
                trend = Trend.UP,
            )
        },
    )
}
