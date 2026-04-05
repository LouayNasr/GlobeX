package io.github.louaynasr.globex.features.rates.domain.usecases

import io.github.louaynasr.globex.core.domain.DataError
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRatesWithCurrencyUseCaseTest {

    private lateinit var useCase: GetRatesWithCurrencyUseCase
    private val ratesRepository: RatesRepository = mockk()
    private val currencyRepository: CurrencyRepository = mockk()

    private val baseCurrency = "USD"
    private val date = "2023-10-26"
    private val yesterdayDate = "2023-10-25"

    @Before
    fun setUp() {
        useCase = GetRatesWithCurrencyUseCase(ratesRepository, currencyRepository)
    }

    @Test
    fun `invoke should return Success with mapped names and trends when all calls succeed`() =
        runTest {
            // Given
            val currencies = listOf(
                Currency("USD", "US Dollar"),
                Currency("EUR", "Euro")
            )
            val ratesToday = listOf(Rate("EUR", "", 0.85, "url", "", Trend.SAME))
            val ratesYesterday = listOf(Rate("EUR", "", 0.80, "url", "", Trend.SAME))

            val successCurrencies = NetworkResult.Success(currencies)
            val successToday =
                NetworkResult.Success(ExchangeRates(1.0, "USD", "url", date, "", ratesToday))
            val successYesterday = NetworkResult.Success(
                ExchangeRates(
                    1.0,
                    "USD",
                    "url",
                    yesterdayDate,
                    "",
                    ratesYesterday
                )
            )

            coEvery { currencyRepository.getCurrencies() } returns successCurrencies
            coEvery { ratesRepository.getExchangeRatesWithBase(baseCurrency) } returns successToday
            coEvery {
                ratesRepository.getExchangeRatesWithDate(
                    date,
                    baseCurrency
                )
            } returns successYesterday

            // When
            val result = useCase(date, baseCurrency)

            // Then
            assertTrue(result is NetworkResult.Success)
            val data = (result as NetworkResult.Success).data
            assertEquals("US Dollar", data.name)
            assertEquals("Euro", data.rates[0].name)
            assertEquals(Trend.UP, data.rates[0].trend)
            assertEquals("6.25%", data.rates[0].changePercentage)
        }

    @Test
    fun `invoke should fallback to code when currency name is not found in map`() = runTest {
        // Given
        coEvery { currencyRepository.getCurrencies() } returns NetworkResult.Success(emptyList()) // No names
        coEvery { ratesRepository.getExchangeRatesWithBase(any()) } returns NetworkResult.Success(
            ExchangeRates(
                1.0,
                "USD",
                "",
                date,
                "",
                listOf(Rate("EUR", "", 0.8, "", "", Trend.SAME))
            )
        )
        coEvery {
            ratesRepository.getExchangeRatesWithDate(
                any(),
                any()
            )
        } returns NetworkResult.Success(
            ExchangeRates(
                1.0,
                "USD",
                "",
                yesterdayDate,
                "",
                listOf(Rate("EUR", "", 0.8, "", "", Trend.SAME))
            )
        )

        // When
        val result = useCase(date, baseCurrency)

        // Then
        val data = (result as NetworkResult.Success).data
        assertEquals("USD", data.name)
        assertEquals("EUR", data.rates[0].name)
    }

    @Test
    fun `invoke should return Currency Error when currency repository fails`() = runTest {
        val error = NetworkResult.Error(DataError.Remote.NO_INTERNET)
        coEvery { currencyRepository.getCurrencies() } returns error

        val result = useCase(date, baseCurrency)

        assertEquals(error, result)
    }

    @Test
    fun `invoke should return Today's Rates even if Yesterday's Rates fail`() = runTest {
        val currencies = listOf(Currency("USD", "US Dollar"), Currency("EUR", "Euro"))
        val ratesToday = listOf(Rate("EUR", "Euro", 0.85, "", "", Trend.SAME))
        val exchangeRatesToday = ExchangeRates(1.0, "USD", "", date, "US Dollar", ratesToday)

        coEvery { currencyRepository.getCurrencies() } returns NetworkResult.Success(currencies)
        coEvery { ratesRepository.getExchangeRatesWithBase(baseCurrency) } returns NetworkResult.Success(
            exchangeRatesToday
        )
        coEvery {
            ratesRepository.getExchangeRatesWithDate(
                date,
                baseCurrency
            )
        } returns NetworkResult.Error(DataError.Remote.UNKNOWN)

        // When
        val result = useCase(date, baseCurrency)

        // Then
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("US Dollar", data.name)
        assertEquals(Trend.SAME, data.rates[0].trend)
        assertEquals("0.00%", data.rates[0].changePercentage)
    }

    @Test
    fun `calculateChange should return Trend SAME if yesterday is null`() {
        val today = Rate("EUR", "Euro", 0.9, "", "", Trend.SAME)
        val result = useCase.calculateChange(today, null)
        assertEquals(Trend.SAME, result.trend)
        assertEquals("0.00%", result.changePercentage)
    }

    @Test
    fun `calculateChange should return Trend SAME if yesterday rate is zero`() {
        val today = Rate("EUR", "Euro", 0.9, "", "", Trend.SAME)
        val yesterday = Rate("EUR", "Euro", 0.0, "", "", Trend.SAME)
        val result = useCase.calculateChange(today, yesterday)
        assertEquals(Trend.SAME, result.trend)
    }

    @Test
    fun `calculateChange should return Trend SAME for changes within 0,01 threshold`() {
        // Change is exactly 0.01% -> 0.80008 vs 0.8 is 0.01%
        val yesterday = Rate("EUR", "Euro", 0.8, "", "", Trend.SAME)
        val today = Rate("EUR", "Euro", 0.80004, "", "", Trend.SAME) // 0.005% change

        val result = useCase.calculateChange(today, yesterday)
        assertEquals(Trend.SAME, result.trend)
        assertEquals("0.00%", result.changePercentage)
    }

    @Test
    fun `calculateChange should return Trend UP for change above 0,01 threshold`() {
        val yesterday = Rate("EUR", "Euro", 1.0, "", "", Trend.SAME)
        val today = Rate("EUR", "Euro", 1.01, "", "", Trend.SAME) // 1% change

        val result = useCase.calculateChange(today, yesterday)
        assertEquals(Trend.UP, result.trend)
        assertEquals("1.00%", result.changePercentage)
    }

    @Test
    fun `calculateChange should return Trend DOWN for negative change below -0,01 threshold`() {
        val yesterday = Rate("EUR", "Euro", 1.0, "", "", Trend.SAME)
        val today = Rate("EUR", "Euro", 0.98, "", "", Trend.SAME) // -2% change

        val result = useCase.calculateChange(today, yesterday)
        assertEquals(Trend.DOWN, result.trend)
        assertEquals("-2.00%", result.changePercentage)
    }
}