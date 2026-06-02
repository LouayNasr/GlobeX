package io.github.louaynasr.globex.DataStore

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val baseCurrencyFlow: Flow<String>
    val converterFirstCurrencyFlow: Flow<String>
    val converterSecondCurrencyFlow: Flow<String>
    val isDarkModeFlow: Flow<Boolean>
    val visibleCurrenciesFlow: Flow<Set<String>>

    suspend fun saveBaseCurrency(currency: String)
    suspend fun saveConverterFirstCurrency(currency: String)
    suspend fun saveConverterSecondCurrency(currency: String)
    suspend fun saveDarkMode(isDarkMode: Boolean)
    suspend fun saveVisibleCurrencies(currencies: Set<String>)
}