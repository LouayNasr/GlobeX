package io.github.louaynasr.globex.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.louaynasr.globex.features.coverter.data.remote.ConverterApiService
import io.github.louaynasr.globex.features.coverter.data.repository.ConverterRepositoryImpl
import io.github.louaynasr.globex.features.coverter.domain.repository.ConverterRepository
import io.github.louaynasr.globex.features.rates.data.remote.CurrencyApiService
import io.github.louaynasr.globex.features.rates.data.remote.RatesApiService
import io.github.louaynasr.globex.features.rates.data.repository.CurrencyRepositoryImpl
import io.github.louaynasr.globex.features.rates.data.repository.RatesRepositoryImpl
import io.github.louaynasr.globex.features.rates.domain.repository.CurrencyRepository
import io.github.louaynasr.globex.features.rates.domain.repository.RatesRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "https://api.frankfurter.dev/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRatesApiService(retrofit: Retrofit): RatesApiService {
        return retrofit.create(RatesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRatesRepository(apiService: RatesApiService): RatesRepository {
        return RatesRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideCurrencyApiService(retrofit: Retrofit): CurrencyApiService {
        return retrofit.create(CurrencyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(currencyApiService: CurrencyApiService): CurrencyRepository {
        return CurrencyRepositoryImpl(currencyApiService)
    }

    @Provides
    @Singleton
    fun provideConverterApiService(retrofit: Retrofit): ConverterApiService {
        return retrofit.create(ConverterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideConverterRepository(converterApiService: ConverterApiService): ConverterRepository {
        return ConverterRepositoryImpl(converterApiService)

    }
}