package io.github.louaynasr.globex.core.presentation.navigtion

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.louaynasr.globex.features.coverter.presentation.ConverterScreen
import io.github.louaynasr.globex.features.rates.presentation.HomeScreen
import io.github.louaynasr.globex.features.settings.presentation.SettingsScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = Route.Home,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember {
        Navigator(navigationState)
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            GlobeXNavigationBar(
                selectedKey = navigationState.topLevelRoute,
                onSelectedKeyChange = { navigator.navigate(it) },
                modifier = modifier,
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = navigator::goBack,
            entries = navigationState.toEntries(
                entryProvider {
                    entry<Route.Home> {
                        HomeScreen()
                    }
                    entry<Route.Converter> {
                        ConverterScreen()
                    }
                    entry<Route.Settings> {
                        SettingsScreen()
                    }
                }
            )
        )
    }
}