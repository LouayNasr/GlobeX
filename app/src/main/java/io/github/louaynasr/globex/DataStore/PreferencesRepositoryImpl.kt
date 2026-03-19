package io.github.louaynasr.globex.DataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.louaynasr.globex.DataStore.PreferencesKeys.BASE_CURRENCY_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object PreferencesKeys {
    val BASE_CURRENCY_KEY = stringPreferencesKey("base_currency")
}


class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override val baseCurrencyFlow: Flow<String> =
        dataStore.data.map { it[BASE_CURRENCY_KEY] ?: "EUR" }

    override suspend fun saveBaseCurrency(currency: String) {
        dataStore.edit { it[BASE_CURRENCY_KEY] = currency }
    }
}