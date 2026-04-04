package io.github.louaynasr.globex.features.rates.data.mappers

import io.github.louaynasr.globex.features.rates.data.dto.ExchangeRatesDto
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend

fun ExchangeRatesDto.toDomain(): ExchangeRates =
    ExchangeRates(
        amount = amount,
        base = base,
        baseFlagUrl = "https://flagcdn.com/80x60/${base.take(2).lowercase()}.png",
        date = date,
        name = "",
        rates = rates
            .map { (currencyCode, value) ->
                Rate(
                    code = currencyCode,
                    name = "",
                    rate = value,
                    flagUrl = "https://flagcdn.com/80x60/${currencyCode.take(2).lowercase()}.png",
                    changePercentage = "0.0",
                    trend = Trend.UP,
                )
            }
    )
