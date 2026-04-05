package io.github.louaynasr.globex.features.rates.presentation

import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.R
import io.github.louaynasr.globex.core.domain.DataError
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.core.presentation.UiText
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.model.ExchangeRates
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.usecases.GetRatesWithCurrencyUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val prefsRepository: PreferencesRepository = mockk()
    private val ratesWithCurrencyUseCase: GetRatesWithCurrencyUseCase = mockk()
    private val currencyRepository: CurrencyRepository = mockk()

    private val testDispatcher = StandardTestDispatcher()
    private val baseCurrencyFlow = MutableStateFlow("EUR")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { prefsRepository.baseCurrencyFlow } returns baseCurrencyFlow

        coEvery { ratesWithCurrencyUseCase.invoke(any(), any()) } returns NetworkResult.Error(
            DataError.Remote.UNKNOWN
        )
        coEvery { currencyRepository.getCurrencies() } returns NetworkResult.Success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = HomeViewModel(prefsRepository, ratesWithCurrencyUseCase, currencyRepository)
    }

    @Test
    fun `initial state should have loading true and default base EUR`() = runTest {
        initViewModel()

        val state = viewModel.homeScreenState
        assertTrue(state.isLoading)
        assertEquals("EUR", state.base)
    }

    @Test
    fun `when baseCurrencyFlow emits, it should update state and fetch rates`() = runTest {
        val exchangeRates = ExchangeRates(1.0, "USD", "url", "2023-10-26", "US Dollar", emptyList())

        coEvery { ratesWithCurrencyUseCase.invoke(any(), "USD") } returns NetworkResult.Success(
            exchangeRates
        )

        initViewModel()
        testDispatcher.scheduler.runCurrent() // Start collection

        baseCurrencyFlow.value = "USD"
        testDispatcher.scheduler.runCurrent() // Trigger collection
        advanceUntilIdle() // Wait for fetch

        assertEquals("USD", viewModel.homeScreenState.base)
        assertEquals("US Dollar", viewModel.homeScreenState.baseName)
    }

    @Test
    fun `fetchRates success updates homeScreenState`() = runTest {
        val exchangeRates = ExchangeRates(1.0, "EUR", "url", "date", "Euro", emptyList())
        coEvery { ratesWithCurrencyUseCase.invoke(any(), "EUR") } returns NetworkResult.Success(
            exchangeRates
        )

        initViewModel()
        viewModel.fetchRates()
        advanceUntilIdle()

        assertEquals("Euro", viewModel.homeScreenState.baseName)
        assertFalse(viewModel.homeScreenState.isLoading)
    }

    @Test
    fun `fetchRates error updates homeScreenState with error message`() = runTest {
        coEvery { ratesWithCurrencyUseCase.invoke(any(), any()) } returns NetworkResult.Error(
            DataError.Remote.NO_INTERNET
        )

        initViewModel()
        viewModel.fetchRates()
        advanceUntilIdle()

        val state = viewModel.homeScreenState
        assertTrue(state.errorMessage is UiText.StringResourceId)
        assertEquals(R.string.error_no_internet, (state.errorMessage as UiText.StringResourceId).id)
    }

    @Test
    fun `fetchRates should set isRefreshing when list is already populated`() = runTest {
        val rates = listOf(Rate("USD", "US Dollar", 1.1, "url", "0%", Trend.SAME))
        val exchangeRates = ExchangeRates(1.0, "EUR", "url", "date", "Euro", rates)

        coEvery { ratesWithCurrencyUseCase.invoke(any(), any()) } coAnswers {
            delay(1000)
            NetworkResult.Success(exchangeRates)
        }

        initViewModel()
        testDispatcher.scheduler.runCurrent()

        // 1. Initial Load
        advanceTimeBy(1001)
        runCurrent()

        // 2. Manual Refresh
        viewModel.fetchRates()
        runCurrent()

        assertTrue(
            "Expected isRefreshing to be true during call",
            viewModel.homeScreenState.isRefreshing
        )

        advanceTimeBy(1001)
        runCurrent()
        assertFalse(viewModel.homeScreenState.isRefreshing)
    }

    @Test
    fun `fetchCurrencies success updates currenciesDialogState`() = runTest {
        val currencies = listOf(Currency("USD", "US Dollar"))
        coEvery { currencyRepository.getCurrencies() } returns NetworkResult.Success(currencies)

        initViewModel()
        viewModel.fetchCurrencies()
        advanceUntilIdle()

        assertEquals(currencies, viewModel.currenciesDialogState.currencyList)
        assertFalse(viewModel.currenciesDialogState.isLoading)
    }

    @Test
    fun `onCurrencyChanged should call prefsRepository saveBaseCurrency`() = runTest {
        coEvery { prefsRepository.saveBaseCurrency(any()) } returns Unit

        initViewModel()
        viewModel.onCurrencyChanged("GBP")
        advanceUntilIdle()

        coVerify { prefsRepository.saveBaseCurrency("GBP") }
    }
}
