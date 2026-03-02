package io.github.louaynasr.globex.core.presentation.navigtion

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Home : Route

    @Serializable
    data object Converter : Route

    @Serializable
    data object Settings : Route
}