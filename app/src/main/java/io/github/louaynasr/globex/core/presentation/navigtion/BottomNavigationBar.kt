package io.github.louaynasr.globex.core.presentation.navigtion

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey

@Composable
fun GlobeXNavigationBar(
    selectedKey: NavKey,
    onSelectedKeyChange: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { (key, value) ->
            NavigationBarItem(
                selected = selectedKey == key,
                onClick = { onSelectedKeyChange(key) },
                icon = {
                    Icon(
                        imageVector = value.icon,
                        contentDescription = stringResource(value.title)
                    )
                },
                label = {
                    Text(stringResource(value.title))
                }
            )
        }
    }
}