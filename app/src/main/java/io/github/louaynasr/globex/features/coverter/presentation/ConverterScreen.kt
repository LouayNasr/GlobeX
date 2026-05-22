package io.github.louaynasr.globex.features.coverter.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.louaynasr.globex.features.coverter.presentation.components.ChartSection
import io.github.louaynasr.globex.features.coverter.presentation.components.ConversionSection
import io.github.louaynasr.globex.features.coverter.presentation.components.ConverterCurrenciesDialog
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            ConverterTopBar(onNavigateBack = onNavigateBack)
        }
    ) {
        ConverterScreenContent(
            state = state,
            dialogState = dialogState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun ConverterScreenContent(
    state: ConverterScreenState,
    dialogState: CurrenciesDialogState,
    onAction: (ConverterActions) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        if (state.showDialog) {
            ConverterCurrenciesDialog(
                state = dialogState,
                onSelect = { item ->
                    if (dialogState.selectedBaseCurrency == state.firstCurrency.code) {
                        onAction(
                            ConverterActions.FirstItemChanged(item)
                        )
                    } else {
                        onAction(
                            ConverterActions.SecondItemChanged(item)
                        )
                    }
                },
                onDismiss = { onAction(ConverterActions.DialogDismiss) }
            )
        }
        ConversionSection(
            firstCurrency = state.firstCurrency,
            secondCurrency = state.secondCurrency,
            onFirstCurrencyChangeRequest = { onAction(ConverterActions.FirstItemClicked) },
            onSecondCurrencyChangeRequest = { onAction(ConverterActions.SecondItemClicked) },
            firstAmount = state.firstAmount,
            secondAmount = state.secondAmount,
            onFirstAmountChanged = { amount -> onAction(ConverterActions.FirstAmountChanged(amount)) },
            onSecondAmountChanged = { amount -> onAction(ConverterActions.SecondAmountChanged(amount)) },
            onSwapCurrencies = { onAction(ConverterActions.OnSwapCurrencies) }
        )

        ChartSection(
            selectedRange = state.selectedDuration,
            historicalRates = state.canvasValues,
            onRangeSelected = { range -> onAction(ConverterActions.DurationChanged(range)) }
        )
    }
}

@Composable
fun ConverterTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Currency Converter",
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        )
    }
}