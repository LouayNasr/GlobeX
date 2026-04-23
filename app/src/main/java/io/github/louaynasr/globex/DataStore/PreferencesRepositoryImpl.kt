package io.github.louaynasr.globex.DataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.louaynasr.globex.DataStore.PreferencesKeys.BASE_CURRENCY_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.CONVERTER_FIRST_CURRENCY_KEY
import io.github.louaynasr.globex.DataStore.PreferencesKeys.CONVERTER_SECOND_CURRENCY_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object PreferencesKeys {
    val BASE_CURRENCY_KEY = stringPreferencesKey("base_currency")
    val CONVERTER_FIRST_CURRENCY_KEY = stringPreferencesKey("converter_first_currency")
    val CONVERTER_SECOND_CURRENCY_KEY = stringPreferencesKey("converter_second_currency")
}


class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override val baseCurrencyFlow: Flow<String> =
        dataStore.data.map { it[BASE_CURRENCY_KEY] ?: "USD" }

    override val converterFirstCurrencyFlow: Flow<String>
        get() = dataStore.data.map { it[CONVERTER_FIRST_CURRENCY_KEY] ?: "USD" }
    override val converterSecondCurrencyFlow: Flow<String>
        get() = dataStore.data.map { it[CONVERTER_SECOND_CURRENCY_KEY] ?: "EUR" }

    override suspend fun saveBaseCurrency(currency: String) {
        dataStore.edit { it[BASE_CURRENCY_KEY] = currency }
    }

    override suspend fun saveConverterFirstCurrency(currency: String) {
        dataStore.edit { it[CONVERTER_FIRST_CURRENCY_KEY] = currency }
    }

    override suspend fun saveConverterSecondCurrency(currency: String) {
        dataStore.edit { it[CONVERTER_SECOND_CURRENCY_KEY] = currency }
    }
}