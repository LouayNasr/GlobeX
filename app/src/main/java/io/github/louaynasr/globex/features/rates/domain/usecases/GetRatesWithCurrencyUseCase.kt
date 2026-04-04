package io.github.louaynasr.globex.features.rates.domain.usecases

import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import javax.inject.Inject


class GetRatesWithCurrencyUseCase @Inject constructor(
    private val ratesRepository: RatesRepository,
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(date: String, baseCurrency: String): NetworkResult<ExchangeRates> {

        val currenciesResult = currencyRepository.getCurrencies()
        val ratesResult = ratesRepository.getExchangeRatesWithBase(baseCurrency)
        val lastDayRatesResult = ratesRepository.getExchangeRatesWithDate(date, baseCurrency)

        return when {
            currenciesResult is NetworkResult.Success && ratesResult is NetworkResult.Success && lastDayRatesResult is NetworkResult.Success -> {
                val currencyMap = currenciesResult.data.associate { it.code to it.name }
                val ratesWithTrend = mapRates(ratesResult.data.rates, lastDayRatesResult.data.rates)

                val mergedData = ratesResult.data.copy(
                    name = currencyMap[ratesResult.data.base] ?: ratesResult.data.base,
                    rates = ratesWithTrend.map { rate ->
                        rate.copy(name = currencyMap[rate.code] ?: rate.code)
                    }
                )

                NetworkResult.Success(mergedData)
            }

            currenciesResult is NetworkResult.Error -> currenciesResult

            else -> ratesResult
        }
    }

    fun mapRates(
        today: List<Rate>,
        yesterday: List<Rate>
    ): List<Rate> {

        val yesterdayMap = yesterday.associateBy { it.code }

        return today.map { todayRate ->
            val yesterdayRate = yesterdayMap[todayRate.code]
            calculateChange(todayRate, yesterdayRate)
        }
    }

    fun calculateChange(
        today: Rate,
        yesterday: Rate?
    ): Rate {
        if (yesterday == null || yesterday.rate == 0.0) {
            return today.toUi("0.00%", Trend.SAME)
        }


        val diff = today.rate - yesterday.rate
        var percent = (diff / yesterday.rate) * 100

        if (percent < 0.01 && percent > -0.01) {
            percent = 0.00
        }

        val trend = when {
            percent > 0.01 -> Trend.UP
            percent < -0.01 -> Trend.DOWN
            else -> Trend.SAME
        }
        val formatedPercent = "%.2f".format(percent) + "%"
        return today.toUi(formatedPercent, trend)
    }

    private fun Rate.toUi(
        percent: String,
        trend: Trend
    ) = Rate(
        code = code,
        name = name,
        rate = rate,
        flagUrl = flagUrl,
        changePercentage = percent,
        trend = trend
    )
}