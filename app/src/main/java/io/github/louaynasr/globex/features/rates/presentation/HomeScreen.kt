package io.github.louaynasr.globex.features.rates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.louaynasr.globex.R
import io.github.louaynasr.globex.core.presentation.components.ErrorScreen
import io.github.louaynasr.globex.core.presentation.components.LoadingScreen
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.presentation.components.BaseCard
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialog
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialogState
import io.github.louaynasr.globex.features.rates.presentation.components.RateItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToManageCurrencies: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state = homeViewModel.homeScreenState.collectAsStateWithLifecycle()
    val dialogState = homeViewModel.currenciesDialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var snackbarJob by remember { mutableStateOf<Job?>(null) }
    val undoLabel = stringResource(id = R.string.undo)

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToManageCurrencies) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Manage Currencies"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        HomeScreenContent(
            state = state.value,
            dialogState = dialogState.value,
            onRetry = homeViewModel::fetchRates,
            onRefresh = homeViewModel::fetchRates,
            fetchCurrencies = homeViewModel::fetchCurrencies,
            onCurrencyChanged = homeViewModel::onCurrencyChanged,
            onRemoveCurrency = { targetRate ->
                homeViewModel.onRemoveCurrency(targetRate.code)
                snackbarJob?.cancel()
                snackbarJob = scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "${targetRate.name} removed",
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        homeViewModel.onUndoRemove(targetRate.code)
                    }
                }
            },
            modifier = modifier.padding(it)
        )
    }
}

@Composable
fun HomeScreenContent(
    state: HomeScreenState,
    dialogState: CurrenciesDialogState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    fetchCurrencies: () -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onRemoveCurrency: (Rate) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading && state.ratesList.isEmpty() -> LoadingScreen(modifier = modifier)
        (state.errorMessage != null) -> ErrorScreen(
            message = state.errorMessage.asString(),
            onRetry = onRetry,
            modifier = modifier
        )

        else -> RatesSuccessContent(
            state = state,
            dialogState = dialogState,
            onRefresh = onRefresh,
            fetchCurrencies = fetchCurrencies,
            onCurrencyChanged = onCurrencyChanged,
            onRemoveCurrency = onRemoveCurrency,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesSuccessContent(
    state: HomeScreenState,
    dialogState: CurrenciesDialogState,
    onRefresh: () -> Unit,
    fetchCurrencies: () -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onRemoveCurrency: (Rate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCurrenciesDialog by remember { mutableStateOf(false) }

    if (showCurrenciesDialog) {
        LaunchedEffect(Unit) {
            fetchCurrencies()
        }
        CurrenciesDialog(
            state = dialogState,
            onSelect = {
                onCurrencyChanged(it)
                showCurrenciesDialog = false
            },
            onDismiss = { showCurrenciesDialog = false },
            modifier = modifier
        )
    }
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            item {
                BaseCard(
                    currencyCode = state.base,
                    currencyName = state.baseName,
                    flagUrl = state.baseFlagUrl,
                    rateValue = "1.00",
                    onClick = { showCurrenciesDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                Text(
                    text = "GLOBAL EXCHANGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    )
                )
            }
            items(
                state.ratesList,
                key = { it.code },
                contentType = { "rate_item_type" }
            ) { rate ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onRemoveCurrency(rate)
                            true
                        } else {
                            it == SwipeToDismissBoxValue.Settled
                        }
                    }
                )

                // When the item reappears (e.g. after Undo), reset the swipe state
                // without triggering confirmValueChange logic.
                LaunchedEffect(Unit) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        dismissState.reset()
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        alpha =
                                            if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                                dismissState.progress
                                            } else 0f
                                    }
                            )
                        }
                    }
                ) {
                    RateItem(rate = rate)
                }
            }
        }
    }
}

