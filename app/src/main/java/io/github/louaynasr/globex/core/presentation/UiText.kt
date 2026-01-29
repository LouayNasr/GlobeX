package io.github.louaynasr.globex.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String): UiText
    class StringResourceId(
        val id: Int,
        val args: Array<Any> = arrayOf()
    ): UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is StringResourceId -> stringResource(id = this.id, formatArgs =this.args)
        }
    }
}