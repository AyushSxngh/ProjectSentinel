package com.sentinel.admin.data.di

import android.content.Context
import com.sentinel.admin.data.remote.ReconnectPolicy
import com.sentinel.admin.data.remote.SequenceGenerator
import com.sentinel.admin.data.remote.api.DeviceApi
import com.sentinel.admin.data.remote.protocol.DeviceUpdateEventMapper
import com.sentinel.admin.data.remote.protocol.MessageSerializer
import com.sentinel.admin.data.remote.websocket.WebSocketDataSource
import com.sentinel.admin.data.repository.AuthRepositoryImpl
import com.sentinel.admin.data.repository.AudioRepositoryImpl
import com.sentinel.admin.data.repository.ConnectionRepositoryImpl
import com.sentinel.admin.data.repository.DeviceRepositoryImpl
import com.sentinel.admin.data.session.SessionPreferencesImpl
import com.sentinel.admin.domain.repository.AudioRepository
import com.sentinel.admin.domain.repository.AuthRepository
import com.sentinel.admin.domain.repository.ConnectionRepository
import com.sentinel.admin.domain.repository.DeviceRepository
import com.sentinel.admin.domain.session.SessionPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing network-layer dependencies for the Admin application.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionPreferences: SessionPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val currentWsUrl = sessionPreferences.serverUrl ?: "wss://projectsentinel-2.onrender.com/ws"
                val httpScheme = if (currentWsUrl.startsWith("wss://")) "https" else "http"
                val withoutScheme = currentWsUrl.removePrefix("wss://").removePrefix("ws://")
                val hostPort = withoutScheme.split("/").first()
                val host = hostPort.split(":").first()
                val port = if (hostPort.contains(":")) {
                    hostPort.split(":")[1].toIntOrNull() ?: if (httpScheme == "https") 443 else 80
                } else {
                    if (httpScheme == "https") 443 else 80
                }
                val newUrl = originalRequest.url.newBuilder()
                    .scheme(httpScheme)
                    .host(host)
                    .port(port)
                    .build()
                chain.proceed(originalRequest.newBuilder().url(newUrl).build())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi,
        sessionPreferences: SessionPreferences
    ): Retrofit {
        val baseUrl = deriveHttpBaseUrl(sessionPreferences.serverUrl)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }

    /**
     * Converts a WebSocket URL to an HTTP base URL.
     * ws://host:port/ws  → http://host:port/
     * wss://host:port/ws → https://host:port/
     */
    private fun deriveHttpBaseUrl(wsUrl: String?): String {
        val effective = if (wsUrl.isNullOrBlank() || wsUrl.contains("localhost")) {
            "wss://projectsentinel-2.onrender.com/ws"
        } else {
            wsUrl
        }
        return try {
            val httpScheme = if (effective.startsWith("wss://")) "https" else "http"
            val withoutScheme = effective
                .removePrefix("wss://")
                .removePrefix("ws://")
            val hostPort = withoutScheme.split("/").first()
            "$httpScheme://$hostPort/"
        } catch (_: Exception) {
            "https://projectsentinel-2.onrender.com/"
        }
    }

    @Provides
    @Singleton
    fun provideWebSocketDataSource(client: OkHttpClient): WebSocketDataSource {
        return WebSocketDataSource(client)
    }

    @Provides
    @Singleton
    fun provideMessageSerializer(moshi: Moshi): MessageSerializer {
        return MessageSerializer(moshi)
    }

    @Provides
    @Singleton
    fun provideSequenceGenerator(): SequenceGenerator {
        return SequenceGenerator()
    }

    @Provides
    @Singleton
    fun provideReconnectPolicy(): ReconnectPolicy {
        return ReconnectPolicy()
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideConnectionRepository(
        webSocketDataSource: WebSocketDataSource,
        messageSerializer: MessageSerializer,
        scope: CoroutineScope
    ): ConnectionRepository {
        return ConnectionRepositoryImpl(webSocketDataSource, messageSerializer, scope)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDeviceUpdateEventMapper(): DeviceUpdateEventMapper {
        return DeviceUpdateEventMapper()
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        deviceApi: DeviceApi,
        authRepository: AuthRepository,
        connectionRepository: ConnectionRepository,
        eventMapper: DeviceUpdateEventMapper,
        moshi: Moshi,
        scope: CoroutineScope
    ): DeviceRepository {
        return DeviceRepositoryImpl(
            deviceApi, authRepository, connectionRepository,
            eventMapper, moshi, scope
        )
    }

    @Provides
    @Singleton
    fun provideSessionPreferences(@ApplicationContext context: Context): SessionPreferences {
        return SessionPreferencesImpl(context)
    }

    @Provides
    @Singleton
    fun provideAudioRepository(
        connectionRepository: ConnectionRepository,
        messageSerializer: MessageSerializer,
        sequenceGenerator: SequenceGenerator
    ): AudioRepository {
        return AudioRepositoryImpl(connectionRepository, messageSerializer, sequenceGenerator)
    }
}
