package tsa.videocall.sdk.listener

import tsa.videocall.sdk.model.config.JanusState
import tsa.videocall.sdk.plugin.JanusPluginName


interface OnJanusListener {

    fun onJanusStateChanged(plugin: JanusPluginName, state: JanusState)

    fun onJanusConnectionChanged(isConnected: Boolean)
}