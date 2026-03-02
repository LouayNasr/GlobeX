package io.github.louaynasr.globex.core.presentation.navigtion

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.louaynasr.globex.R

data class BottomNavItem(
    val icon: ImageVector,
    @StringRes val title: Int,
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.Home to BottomNavItem(
        icon = Icons.Outlined.Home,
        title = R.string.home
    ),
    Route.Converter to BottomNavItem(
        icon = Icons.AutoMirrored.Outlined.CompareArrows,
        title = R.string.converter
    ),
    Route.Settings to BottomNavItem(
        icon = Icons.Outlined.Settings,
        title = R.string.settings
    )
)
