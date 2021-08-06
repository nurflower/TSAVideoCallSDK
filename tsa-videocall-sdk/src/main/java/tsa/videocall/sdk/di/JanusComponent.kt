package tsa.videocall.sdk.di

import dagger.Component
import tsa.videocall.sdk.JanusManager
import tsa.videocall.sdk.di.qualifier.JanusEchoPluginQualifier
import tsa.videocall.sdk.di.qualifier.JanusVideoRoomQualifier
import tsa.videocall.sdk.plugin.JanusPlugin
import javax.inject.Singleton

@Singleton
@Component(modules = [JanusModule::class])
internal interface JanusComponent {

    fun inject(janusManager: JanusManager)

    @JanusEchoPluginQualifier
    fun provideJanusEchoPlugin(): JanusPlugin

    @JanusVideoRoomQualifier
    fun provideJanusVideoRoomPlugin(): JanusPlugin

    @Component.Factory
    interface Factory {
        fun janusComponent(janusModule: JanusModule): JanusComponent
    }

}