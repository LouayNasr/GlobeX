package io.github.louaynasr.globex.features.rates.data.mappers

import io.github.louaynasr.globex.features.rates.data.dto.CurrencyDto
import io.github.louaynasr.globex.features.rates.domain.model.Currency

fun List<CurrencyDto>.toDomain(): List<Currency> {
    return map { dto ->
        Currency(
            code = dto.code,
            name = dto.name,
            symbol = dto.symbol,
        )
    }
}