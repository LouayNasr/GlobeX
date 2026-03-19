package io.github.louaynasr.globex.features.rates.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.louaynasr.globex.R
import io.github.louaynasr.globex.core.presentation.components.ErrorScreen
import io.github.louaynasr.globex.core.presentation.components.LoadingScreen
import io.github.louaynasr.globex.features.rates.presentation.components.BaseCard
import io.github.louaynasr.globex.features.rates.presentation.components.CurrenciesDialog
import io.github.louaynasr.globex.features.rates.presentation.components.RateItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        HomeScreenContent(
            viewModel = homeViewModel,
            modifier = modifier.padding(it),
            homeViewModel.homeScreenState,
            onRetry = homeViewModel::fetchRates
        )
    }
}

@Composable
fun HomeScreenContent(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    state: HomeScreenState = HomeScreenState(),
    onRetry: () -> Unit,
) {
    when {
        state.isLoading -> LoadingScreen(modifier = modifier)
        (state.errorMessage != null) -> ErrorScreen(
            message = state.errorMessage.asString(),
            onRetry = onRetry,
            modifier = modifier
        )

        else -> RatesSuccessContent(
            viewModel = viewModel,
            state = state,
            modifier = modifier
        )
    }
}

@Composable
fun RatesSuccessContent(
    viewModel: HomeViewModel,
    state: HomeScreenState,
    modifier: Modifier = Modifier
) {
    var showCurrenciesDialog by remember { mutableStateOf(false) }

    if (showCurrenciesDialog) {
        CurrenciesDialog(
            viewModel = viewModel,
            onSelect = {
                viewModel.onCurrencyChanged(it)
                showCurrenciesDialog = false
            },
            onDismiss = { showCurrenciesDialog = false },
            modifier = modifier
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BaseCard(
                currencyCode = state.base,
                currencyName = state.baseName,
                flagUrl = state.baseFlagUrl,
                rateValue = "1.00",
                onClick = { showCurrenciesDialog = true }
            )
        }
        item {
            Text(
                text = "GLOBAL EXCHANGE",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        items(state.ratesList) { rate ->
            RateItem(rate = rate)
        }
    }
}

