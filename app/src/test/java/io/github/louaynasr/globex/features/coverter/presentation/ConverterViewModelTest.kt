package io.github.louaynasr.globex.features.coverter.presentation

import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.core.domain.DataError
import io.github.louaynasr.globex.core.domain.NetworkResult
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterExchangeRates
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate
import io.github.louaynasr.globex.features.coverter.domain.repository.ConverterRepository
import io.github.louaynasr.globex.features.coverter.domain.usecases.GetConverterRatesUseCase
import io.github.louaynasr.globex.features.coverter.presentation.components.TimeRange
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ConverterViewModelTest {

    private lateinit var viewModel: ConverterViewModel
    private val prefsRepository: PreferencesRepository = mockk()
    private val converterRepository: ConverterRepository = mockk()
    private val converterRatesUseCase: GetConverterRatesUseCase = mockk()
    private val currenciesRepository: CurrencyRepository = mockk()

    private val testDispatcher = StandardTestDispatcher()

    private val firstCurrencyFlow = MutableStateFlow("USD")
    private val secondCurrencyFlow = MutableStateFlow("EUR")

    private val mockRates = listOf(
        ConverterRate("USD", "US Dollar", 1.0, "usd_flag"),
        ConverterRate("EUR", "Euro", 0.85, "eur_flag"),
        ConverterRate("GBP", "British Pound", 0.75, "gbp_flag")
    )

    private val mockExchangeRates = ConverterExchangeRates(
        amount = 1.0,
        base = "USD",
        baseFlagUrl = "usd_flag",
        date = "2023-10-26",
        name = "US Dollar",
        rates = mockRates
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)

        every { prefsRepository.converterFirstCurrencyFlow } returns firstCurrencyFlow
        every { prefsRepository.converterSecondCurrencyFlow } returns secondCurrencyFlow

        coEvery { converterRatesUseCase(any()) } returns NetworkResult.Success(mockExchangeRates)
        coEvery {
            converterRepository.getConversionHistory(
                any(),
                any(),
                any(),
                any()
            )
        } returns NetworkResult.Success(emptyList())
        coEvery { currenciesRepository.getCurrencies() } returns NetworkResult.Success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = ConverterViewModel(
            prefsRepository,
            converterRepository,
            converterRatesUseCase,
            currenciesRepository
        )
    }

    @Test
    fun `init should observe currencies and fetch rates`() = runTest {
        initViewModel()
        testDispatcher.scheduler.runCurrent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("USD", state.firstCurrency.code)
        assertEquals("EUR", state.secondCurrency.code)
        assertEquals(mockRates, state.ratesList)
        assertEquals("1.0", state.firstAmount)
        assertEquals("0.85", state.secondAmount) // 1.0 * 0.85

        coVerify { converterRatesUseCase("USD") }
        coVerify { converterRepository.getConversionHistory(any(), any(), "USD", "EUR") }
    }

    @Test
    fun `when first amount changes, second amount is updated correctly`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.FirstAmountChanged("10"))

        // Rate EUR is 0.85. 10 * 0.85 = 8.50
        assertEquals("10", viewModel.uiState.value.firstAmount)
        assertEquals("8.50", viewModel.uiState.value.secondAmount)
    }

    @Test
    fun `when second amount changes, first amount is updated correctly`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.SecondAmountChanged("8.5"))

        // Rate EUR is 0.85. 8.5 / 0.85 = 10.00
        assertEquals("8.5", viewModel.uiState.value.secondAmount)
        assertEquals("10.00", viewModel.uiState.value.firstAmount)
    }

    @Test
    fun `onSwapCurrencies should call prefsRepository with swapped codes`() = runTest {
        coEvery { prefsRepository.saveConverterFirstCurrency(any()) } returns Unit
        coEvery { prefsRepository.saveConverterSecondCurrency(any()) } returns Unit

        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.OnSwapCurrencies)
        advanceUntilIdle()

        coVerify { prefsRepository.saveConverterFirstCurrency("EUR") }
        coVerify { prefsRepository.saveConverterSecondCurrency("USD") }
    }

    @Test
    fun `FirstItemClicked updates dialog state and shows dialog`() = runTest(testDispatcher) {
        val currencies = listOf(Currency("USD", "US Dollar", "$"), Currency("EUR", "Euro", "€"))
        coEvery { currenciesRepository.getCurrencies() } returns NetworkResult.Success(currencies)

        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.FirstItemClicked)

        assertTrue(viewModel.uiState.value.showDialog)
        assertEquals("USD", viewModel.dialogUiState.value.selectedBaseCurrency)
        assertTrue(viewModel.dialogUiState.value.isLoading)

        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.dialogUiState.value.isLoading)
        assertEquals(currencies, viewModel.dialogUiState.value.currencyList)
    }

    @Test
    fun `SecondItemClicked updates dialog state and shows dialog`() = runTest(testDispatcher) {
        val currencies = listOf(Currency("USD", "US Dollar", "$"), Currency("EUR", "Euro", "€"))
        coEvery { currenciesRepository.getCurrencies() } returns NetworkResult.Success(currencies)

        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.SecondItemClicked)

        assertTrue(viewModel.uiState.value.showDialog)
        assertEquals("EUR", viewModel.dialogUiState.value.selectedBaseCurrency)
        assertTrue(viewModel.dialogUiState.value.isLoading)

        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.dialogUiState.value.isLoading)
        assertEquals(currencies, viewModel.dialogUiState.value.currencyList)
    }

    @Test
    fun `DurationChanged updates state and fetches history`() = runTest {
        initViewModel()
        advanceUntilIdle()

        val historicalRates = listOf(HistoricalRate("2023-10-25", 0.84))
        coEvery {
            converterRepository.getConversionHistory(
                any(),
                any(),
                "USD",
                "EUR"
            )
        } returns NetworkResult.Success(historicalRates)

        viewModel.onAction(ConverterActions.DurationChanged(TimeRange.THREE_MONTHS))
        advanceUntilIdle()

        assertEquals(TimeRange.THREE_MONTHS, viewModel.uiState.value.selectedDuration)
        assertEquals(historicalRates, viewModel.uiState.value.canvasValues)
    }

    @Test
    fun `FirstItemChanged saves to prefs and hides dialog`() = runTest {
        coEvery { prefsRepository.saveConverterFirstCurrency(any()) } returns Unit

        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.FirstItemChanged("GBP"))
        advanceUntilIdle()

        coVerify { prefsRepository.saveConverterFirstCurrency("GBP") }
        assertFalse(viewModel.uiState.value.showDialog)
    }

    @Test
    fun `SecondItemChanged saves to prefs and hides dialog`() = runTest {
        coEvery { prefsRepository.saveConverterSecondCurrency(any()) } returns Unit

        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.SecondItemChanged("GBP"))
        advanceUntilIdle()

        coVerify { prefsRepository.saveConverterSecondCurrency("GBP") }
        assertFalse(viewModel.uiState.value.showDialog)
    }

    @Test
    fun `DialogDismiss hides dialog`() = runTest {
        initViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConverterActions.FirstItemClicked)
        assertTrue(viewModel.uiState.value.showDialog)

        viewModel.onAction(ConverterActions.DialogDismiss)
        assertFalse(viewModel.uiState.value.showDialog)
    }

    @Test
    fun `when first currency code changes in prefs, fetchRates is called`() = runTest {
        initViewModel()
        advanceUntilIdle()

        // Change prefs
        val newExchangeRates =
            ConverterExchangeRates(1.0, "GBP", "gbp_flag", "date", "British Pound", mockRates)
        coEvery { converterRatesUseCase("GBP") } returns NetworkResult.Success(newExchangeRates)

        firstCurrencyFlow.value = "GBP"
        advanceUntilIdle()

        coVerify { converterRatesUseCase("GBP") }
        assertEquals("GBP", viewModel.uiState.value.firstCurrency.code)
    }

    @Test
    fun `when second currency code changes in prefs, updateSecondCurrency is called`() = runTest {
        initViewModel()
        advanceUntilIdle()

        // secondCurrency was EUR (0.85). Now change to GBP (0.75) in prefs
        secondCurrencyFlow.value = "GBP"
        advanceUntilIdle()

        assertEquals("GBP", viewModel.uiState.value.secondCurrency.code)
        assertEquals(0.75, viewModel.uiState.value.secondCurrency.rate, 0.0)
        // First amount was 1.0, so second amount should be updated to 0.75
        assertEquals("0.75", viewModel.uiState.value.secondAmount)

        // Should NOT fetch rates again for base change if only second changed
        coVerify(exactly = 1) { converterRatesUseCase("USD") } // 1 from init
    }

    @Test
    fun `fetchCurrencies error updates dialog error state`() = runTest {
        coEvery { currenciesRepository.getCurrencies() } returns NetworkResult.Error(DataError.Remote.NO_INTERNET)

        initViewModel()
        viewModel.fetchCurrencies()
        advanceUntilIdle()

        assertFalse(viewModel.dialogUiState.value.isLoading)
        assertTrue(viewModel.dialogUiState.value.errorMessage != null)
    }

    @Test
    fun `invalid amount input does not update state with invalid value but uses validation logic`() =
        runTest {
            initViewModel()
            advanceUntilIdle()

            // Try to input something invalid (e.g. multiple dots)
            // validateCurrencyInput should handle this.
            viewModel.onAction(ConverterActions.FirstAmountChanged("10.5.5"))

            // Assuming current was 1.0 (from init state, but wait, init recalculates)
            // Actually FirstAmountChanged uses _uiState.value.firstAmount as currentText
            // Initially it's 1.0.
            // 10.5.5 is invalid, so it should stay 1.0
            assertEquals("1.0", viewModel.uiState.value.firstAmount)
        }
}
