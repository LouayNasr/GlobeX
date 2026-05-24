package io.github.louaynasr.globex.features.rates.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(1)
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
        // If the test still hangs, we can try to disable auto-advance
        // composeRule.mainClock.autoAdvance = false
    }

    @Test
    fun loadingState_isShown_whenStateIsLoading() {
        val loadingState = HomeScreenState(isLoading = true)
        composeRule.setContent {
            HomeScreenContent(
                state = loadingState,
                dialogState = CurrenciesDialogState(),
                onRetry = {},
                onRefresh = {},
                fetchCurrencies = {},
                onCurrencyChanged = {}
            )
        }

        // Use a timeout to prevent 10-minute hangs
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithTag("loadingScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("loadingScreen").assertIsDisplayed()
    }

    @Test
    fun ratesList_isDisplayed_whenDataIsProvided() {
        val testRates = listOf(
            Rate(
                code = "USD",
                name = "US Dollar",
                rate = 1.1,
                flagUrl = "",
                changePercentage = "0.5%",
                trend = Trend.UP
            ),
            Rate(
                code = "GBP",
                name = "British Pound",
                rate = 0.85,
                flagUrl = "",
                changePercentage = "-0.2%",
                trend = Trend.DOWN
            )
        )
        val successState = HomeScreenState(
            isLoading = false,
            base = "EUR",
            baseName = "Euro",
            ratesList = testRates
        )

        composeRule.setContent {
            HomeScreenContent(
                state = successState,
                dialogState = CurrenciesDialogState(),
                onRetry = {},
                onRefresh = {},
                fetchCurrencies = {},
                onCurrencyChanged = {}
            )
        }

        composeRule.onNodeWithText("US Dollar").assertIsDisplayed()
        composeRule.onNodeWithText("British Pound").assertIsDisplayed()
        composeRule.onNodeWithText("EUR").assertIsDisplayed()
    }

    @Test
    fun clickingBaseCard_triggersFetchCurrencies() {
        val onFetchCurrencies = mockk<() -> Unit>(relaxed = true)
        val state = HomeScreenState(isLoading = false, base = "EUR")

        composeRule.setContent {
            HomeScreenContent(
                state = state,
                dialogState = CurrenciesDialogState(),
                onRetry = {},
                onRefresh = {},
                fetchCurrencies = onFetchCurrencies,
                onCurrencyChanged = {}
            )
        }

        composeRule.onNodeWithText("EUR").performClick()

        verify { onFetchCurrencies() }
    }

    @Test
    fun selectingCurrencyInDialog_triggersCallback() {
        val onCurrencyChanged = mockk<(String) -> Unit>(relaxed = true)
        val currencies = listOf(Currency("USD", "US Dollar"))

        val dialogState = CurrenciesDialogState(
            isLoading = false,
            currencyList = currencies
        )

        composeRule.setContent {
            HomeScreenContent(
                state = HomeScreenState(isLoading = false, base = "EUR"),
                dialogState = dialogState,
                onRetry = {},
                onRefresh = {},
                fetchCurrencies = {},
                onCurrencyChanged = onCurrencyChanged
            )
        }

        // Open the dialog first by clicking the base card
        composeRule.onNodeWithText("EUR").performClick()

        // Now select the currency from the dialog
        composeRule.onNodeWithText("US Dollar").performClick()

        verify { onCurrencyChanged("USD") }
    }
}