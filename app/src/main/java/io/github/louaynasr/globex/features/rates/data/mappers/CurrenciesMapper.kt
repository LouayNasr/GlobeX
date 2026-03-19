package io.github.louaynasr.globex.features.rates.data.mappers

import io.github.louaynasr.globex.features.rates.data.dto.CurrenciesDto
import io.github.louaynasr.globex.features.rates.domain.model.Currency

fun CurrenciesDto.toDomain(): List<Currency> {
    return this.map { (code, name) ->
        Currency(
            code = code,
            name = name
        )
    }
}