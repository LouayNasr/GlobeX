package io.github.louaynasr.globex.features.rates.domain.usecases

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import javax.inject.Inject


class GetRatesWithCurrencyUseCase @Inject constructor(
    private val ratesRepository: RatesRepository,
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(baseCurrency: String): NetworkResult<ExchangeRates> {

        val currenciesResult = currencyRepository.getCurrencies()
        val ratesResult = ratesRepository.getExchangeRatesWithBase(baseCurrency)

        return when {
            currenciesResult is NetworkResult.Success && ratesResult is NetworkResult.Success -> {
                val currencyMap = currenciesResult.data.associate { it.code to it.name }

                val mergedData = ratesResult.data.copy(
                    name = currencyMap[ratesResult.data.base] ?: ratesResult.data.base,
                    rates = ratesResult.data.rates.map { rate ->
                        rate.copy(name = currencyMap[rate.code] ?: rate.code)
                    }
                )

                NetworkResult.Success(mergedData)
            }

            currenciesResult is NetworkResult.Error -> currenciesResult

            else -> ratesResult
        }
    }
}