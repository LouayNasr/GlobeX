package io.github.louaynasr.globex.DataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.louaynasr.globex.DataStore.PreferencesKeys.BASE_CURRENCY_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.CONVERTER_FIRST_CURRENCY_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.CONVERTER_SECOND_CURRENCY_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.DARK_MODE_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.VISIBLE_CURRENCIES_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object PreferencesKeys {
    val BASE_CURRENCY_KEY = stringPreferencesKey("base_currency")
    val CONVERTER_FIRST_CURRENCY_KEY = stringPreferencesKey("converter_first_currency")
    val CONVERTER_SECOND_CURRENCY_KEY = stringPreferencesKey("converter_second_currency")
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    val VISIBLE_CURRENCIES_KEY = stringSetPreferencesKey("visible_currencies")
}


class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override val baseCurrencyFlow: Flow<String> =
        dataStore.data.map { it[BASE_CURRENCY_KEY] ?: "USD" }

    override val converterFirstCurrencyFlow: Flow<String> =
        dataStore.data.map { it[CONVERTER_FIRST_CURRENCY_KEY] ?: "USD" }

    override val converterSecondCurrencyFlow: Flow<String> =
        dataStore.data.map { it[CONVERTER_SECOND_CURRENCY_KEY] ?: "EUR" }

    override val isDarkModeFlow: Flow<Boolean> =
        dataStore.data.map { it[DARK_MODE_KEY] ?: false }

    override val visibleCurrenciesFlow: Flow<Set<String>> =
        dataStore.data.map { it[VISIBLE_CURRENCIES_KEY] ?: emptySet() }

    override suspend fun saveBaseCurrency(currency: String) {
        dataStore.edit { it[BASE_CURRENCY_KEY] = currency }
    }

    override suspend fun saveConverterFirstCurrency(currency: String) {
        dataStore.edit { it[CONVERTER_FIRST_CURRENCY_KEY] = currency }
    }

    override suspend fun saveConverterSecondCurrency(currency: String) {
        dataStore.edit { it[CONVERTER_SECOND_CURRENCY_KEY] = currency }
    }

    override suspend fun saveDarkMode(isDarkMode: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = isDarkMode }
    }

    override suspend fun saveVisibleCurrencies(currencies: Set<String>) {
        dataStore.edit { it[VISIBLE_CURRENCIES_KEY] = currencies }
    }
}