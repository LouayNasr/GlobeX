package io.github.louaynasr.globex.features.rates.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
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
                        val direction = dismissState.dismissDirection
                        if (direction == SwipeToDismissBoxValue.EndToStart) {
                            val isThresholdReached =
                                dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                            val haptic = LocalHapticFeedback.current

                            LaunchedEffect(isThresholdReached) {
                                if (isThresholdReached) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }

                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val width = constraints.maxWidth.toFloat()
                                val offset =
                                    runCatching { dismissState.requireOffset() }.getOrDefault(0f)
                                val progress = (-offset / width).coerceIn(0f, 1f)

                                val backgroundColor by animateColorAsState(
                                    targetValue = if (isThresholdReached) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer.copy(
                                            alpha = (progress * 0.8f).coerceIn(0f, 1f)
                                        )
                                    },
                                    label = "swipe_color"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(backgroundColor)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    val iconScale =
                                        lerp(0.6f, 1.2f, (progress * 2).coerceIn(0f, 1f))
                                    val iconAlpha = (progress * 3).coerceIn(0f, 1f)

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = if (isThresholdReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                scaleX = if (isThresholdReached) 1.2f else iconScale
                                                scaleY = if (isThresholdReached) 1.2f else iconScale
                                                alpha = if (isThresholdReached) 1f else iconAlpha
                                            }
                                    )
                                }
                            }
                        }
                    }
                ) {
                    RateItem(rate = rate)
                }
            }
        }
    }
}

