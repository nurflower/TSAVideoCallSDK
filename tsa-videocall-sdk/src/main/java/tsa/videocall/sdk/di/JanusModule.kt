package tsa.videocall.sdk.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import tsa.videocall.sdk.BuildConfig
import tsa.videocall.sdk.di.qualifier.JanusEchoPluginQualifier
import tsa.videocall.sdk.di.qualifier.JanusVideoRoomQualifier
import tsa.videocall.sdk.plugin.JanusPlugin
import tsa.videocall.sdk.plugin.echo.JanusEchoPlugin
import tsa.videocall.sdk.plugin.videoroom.JanusVideoRoomPlugin
import tsa.videocall.sdk.utils.JanusMoshiAdapters
import tsa.videocall.sdk.websocket.JanusWSClient
import tsa.videocall.sdk.websocket.JanusWSClientImpl
import java.util.concurrent.TimeUnit

@Module
internal class JanusModule(private val context: Context) {

    @Provides
    @JanusEchoPluginQualifier
    fun provideJanusEchoPlugin(janusWSClient: JanusWSClient): JanusPlugin =
        JanusEchoPlugin(janusWSClient)


    @Provides
    @JanusVideoRoomQualifier
    fun provideJanusVideoRoomPlugin(janusWSClient: JanusWSClient, moshi: Moshi): JanusPlugin =
        JanusVideoRoomPlugin(janusWSClient, moshi)

    @Provides
    fun provideMoshi() = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(JanusMoshiAdapters)
        .build()

    @Provides
    fun provideJanusWSClient(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): JanusWSClient = JanusWSClientImpl(context, okHttpClient, moshi)

    @Provides
    fun provideJanusWSOkHttp(
        logging: HttpLoggingInterceptor
    ) = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .addHeader("Content-type", "application/json")
                .build()
        }
        .retryOnConnectionFailure(true)
        .pingInterval(5000, TimeUnit.SECONDS)
        .build()

    @Provides
    fun provideJanusWSOkHttpLogging() = HttpLoggingInterceptor()
        .apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
}