package com.safeNest.demo.app.di

import com.uney.core.network.api.configs.NetworkConfig
import com.uney.core.network.api.provider.DefaultHeaderProvider
import com.uney.core.network.api.provider.RefreshTokenProvider
import com.uney.core.network.api.provider.SignSignatureProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.JsonObject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = "https://demo-safenest-usecase.qualgo.dev",
            enableLogging = true
        )
    }

    @Provides
    @Singleton
    fun provideDefaultHeadersProvider(): DefaultHeaderProvider = object : DefaultHeaderProvider {
        override fun getDefaultHeaders(): Map<String, String> = mapOf()
    }

    @Provides
    @Singleton
    fun provideRefreshConfigProvider(): RefreshTokenProvider = object : RefreshTokenProvider {
        override fun getRefreshPath(): String = ""
        override suspend fun getRefreshBody(): Map<String, String> = mapOf()
        override fun getRefreshHeaders(): Map<String, String> = mapOf()
    }

    @Provides
    @Singleton
    fun provideSignSignatureProvider(): SignSignatureProvider = object : SignSignatureProvider {
        override fun fromJsonObject(data: JsonObject): String = ""
        override fun fromMap(data: Map<String, String>): String = ""
    }
}