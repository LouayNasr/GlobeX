package io.github.louaynasr.globex.features.coverter.domain.usecases

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterExchangeRates
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import javax.inject.Inject

class GetConverterRatesUseCase @Inject constructor(
    private val ratesRepository: RatesRepository,
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(baseCurrency: String): NetworkResult<ConverterExchangeRates> {

        val currenciesResult = currencyRepository.getCurrencies()
        val ratesResult = ratesRepository.getExchangeRatesWithBase(baseCurrency)

        return when {
            currenciesResult is NetworkResult.Success && ratesResult is NetworkResult.Success -> {
                val currencyMap = currenciesResult.data.associate { it.code to it.name }

                val converterRates = ratesResult.data.rates.map { rate ->
                    ConverterRate(
                        code = rate.code,
                        name = currencyMap[rate.code] ?: rate.code,
                        rate = rate.rate,
                        flagUrl = rate.flagUrl
                    )
                }

                val mergedData = ConverterExchangeRates(
                    amount = ratesResult.data.amount,
                    base = ratesResult.data.base,
                    baseFlagUrl = ratesResult.data.baseFlagUrl,
                    date = ratesResult.data.date,
                    name = currencyMap[ratesResult.data.base] ?: ratesResult.data.base,
                    rates = converterRates
                )

                NetworkResult.Success(mergedData)
            }

            currenciesResult is NetworkResult.Error -> currenciesResult

            else -> ratesResult
        } as NetworkResult<ConverterExchangeRates>
    }
}