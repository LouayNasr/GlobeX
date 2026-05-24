package io.github.louaynasr.globex.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.github.louaynasr.globex.DataStore.PreferencesRepository
import io.github.louaynasr.globex.features.coverter.domain.repository.ConverterRepository
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AppModule::class,
        PreferencesRepositoryModule::class
    ]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideRatesRepository(): RatesRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideCurrencyRepository(): CurrencyRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideConverterRepository(): ConverterRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun providePreferencesRepository(): PreferencesRepository = mockk(relaxed = true) {
        every { baseCurrencyFlow } returns flowOf("USD")
    }
}