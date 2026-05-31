package io.github.louaynasr.globex.features.coverter.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate
import io.github.louaynasr.globex.features.coverter.presentation.components.TimeRange
import io.github.louaynasr.globex.features.rates.domain.model.Currency
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private val initialState = ConverterScreenState(
        firstCurrency = ConverterRate(code = "USD", name = "US Dollar", rate = 1.0, flagUrl = ""),
        secondCurrency = ConverterRate(code = "EUR", name = "Euro", rate = 0.92, flagUrl = ""),
        firstAmount = "10.0",
        secondAmount = "9.2",
        selectedDuration = TimeRange.ONE_MONTH,
        showDialog = false,
    )

    private val dialogState = CurrenciesDialogState()

    @Test
    fun initialState_isDisplayedCorrectly() {
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = {},
                modifier = Modifier
            )
        }

        // Verify currency codes
        composeTestRule.onNodeWithText("USD").assertIsDisplayed()
        composeTestRule.onNodeWithText("EUR").assertIsDisplayed()

        // Verify amounts
        composeTestRule.onNodeWithTag("first_amount_field").assertTextContains("10.0")
        composeTestRule.onNodeWithTag("second_amount_field").assertTextContains("9.2")

        // Verify conversion rate text
        composeTestRule.onNodeWithText("1 USD = 0.92 EUR").assertIsDisplayed()
    }

    @Test
    fun clickingFirstCurrency_triggersFirstItemClickedAction() {
        var actionReceived: ConverterActions? = null
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = { actionReceived = it },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithTag("first_currency_selector")
            .performClick()

        assertTrue(actionReceived is ConverterActions.FirstItemClicked)
    }

    @Test
    fun clickingSecondCurrency_triggersSecondItemClickedAction() {
        var actionReceived: ConverterActions? = null
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = { actionReceived = it },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithTag("second_currency_selector")
            .performClick()

        assertTrue(actionReceived is ConverterActions.SecondItemClicked)
    }

    @Test
    fun typingInFirstAmount_triggersFirstAmountChangedAction() {
        var actionReceived: ConverterActions? = null
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = { actionReceived = it },
                modifier = Modifier
            )
        }

        val newAmount = "20.0"
        composeTestRule.onNodeWithTag("first_amount_field").performTextInput(newAmount)

        assertTrue(actionReceived is ConverterActions.FirstAmountChanged)
    }

    @Test
    fun clickingSwapButton_triggersOnSwapCurrenciesAction() {
        var actionReceived: ConverterActions? = null
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = { actionReceived = it },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithContentDescription("Swap").performClick()

        assertTrue(actionReceived is ConverterActions.OnSwapCurrencies)
    }

    @Test
    fun clickingRangeButton_triggersDurationChangedAction() {
        var actionReceived: ConverterActions? = null
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState,
                dialogState = dialogState,
                onAction = { actionReceived = it },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithTag("range_button_3m").performClick()

        val action = actionReceived as? ConverterActions.DurationChanged
        assertTrue(action != null && action.code == TimeRange.THREE_MONTHS)
    }

    @Test
    fun whenShowDialogIsTrue_dialogIsDisplayed() {
        composeTestRule.setContent {
            ConverterScreenContent(
                state = initialState.copy(showDialog = true),
                dialogState = dialogState.copy(
                    currencyList = listOf(
                        Currency(
                            "USD",
                            "US Dollar",
                            "$"
                        )
                    )
                ),
                onAction = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("US Dollar").assertIsDisplayed()
    }

    @Test
    fun clickingBackButton_triggersOnNavigateBack() {
        var backClicked = false
        composeTestRule.setContent {
            ConverterTopBar { backClicked = true }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }
}
