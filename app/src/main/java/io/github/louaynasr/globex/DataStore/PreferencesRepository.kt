package io.github.louaynasr.globex.DataStore

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val baseCurrencyFlow: Flow<String>

    suspend fun saveBaseCurrency(currency: String)
}