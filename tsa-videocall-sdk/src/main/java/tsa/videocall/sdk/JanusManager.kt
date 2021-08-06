package tsa.videocall.sdk

import android.app.Application
import android.content.Context
import tsa.videocall.sdk.di.DaggerJanusComponent
import tsa.videocall.sdk.di.JanusModule
import tsa.videocall.sdk.di.qualifier.JanusEchoPluginQualifier
import tsa.videocall.sdk.di.qualifier.JanusVideoRoomQualifier
import tsa.videocall.sdk.plugin.JanusPlugin
import javax.inject.Inject

class JanusManager {

    val echoPlugin: JanusPlugin
        get() = _echoPlugin


    val videoRoomPlugin: JanusPlugin
        get() = _videoRoomPlugin

    @Inject
    @JanusEchoPluginQualifier
    internal lateinit var _echoPlugin: JanusPlugin


    @Inject
    @JanusVideoRoomQualifier
    internal lateinit var _videoRoomPlugin: JanusPlugin

    fun init(applicationContext: Context) {
        if (applicationContext !is Application) throw IllegalArgumentException()
        DaggerJanusComponent.factory()
            .janusComponent(JanusModule(applicationContext))
            .inject(this)
    }

    companion object {
        @Volatile
        private var instance: JanusManager? = null

        fun getInstance(): JanusManager {
            return instance ?: synchronized(this) {
                instance ?: JanusManager()
                    .also {
                        instance = it
                    }
            }
        }
    }
}