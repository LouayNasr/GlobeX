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
        val currencies = when (val currenciesResult = currencyRepository.getCurrencies()) {
            is NetworkResult.Success -> currenciesResult.data
            is NetworkResult.Error -> return currenciesResult
        }

        val ratesResult = ratesRepository.getExchangeRatesWithBase(baseCurrency)
        val ratesToday = when (ratesResult) {
            is NetworkResult.Success -> ratesResult.data
            is NetworkResult.Error -> return ratesResult
        }

        val currencyMap = currencies.associate { it.code to it.name }

        val lastDayRatesResult = ratesRepository.getExchangeRatesWithDate(date, baseCurrency)

        val yesterdayRates = if (lastDayRatesResult is NetworkResult.Success) {
            lastDayRatesResult.data.rates
        } else {
            emptyList()
        }

        val ratesWithTrend = mapRates(ratesToday.rates, yesterdayRates)

        val mergedData = ratesToday.copy(
            name = currencyMap[ratesToday.base] ?: ratesToday.base,
            rates = ratesWithTrend.map { rate ->
                rate.copy(name = currencyMap[rate.code] ?: rate.code)
            }
        )

        return NetworkResult.Success(mergedData)
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

        val formattedPercent = "%.2f".format(percent) + "%"
        return today.toUi(formattedPercent, trend)
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