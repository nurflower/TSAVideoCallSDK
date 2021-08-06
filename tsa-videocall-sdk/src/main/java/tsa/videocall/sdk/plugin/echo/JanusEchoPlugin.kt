package tsa.videocall.sdk.plugin.echo

import android.util.Log
import tsa.videocall.sdk.model.config.JanusCommand
import tsa.videocall.sdk.model.config.JanusCommandName
import tsa.videocall.sdk.model.config.JanusEventName
import tsa.videocall.sdk.model.request.JanusBodyRequest
import tsa.videocall.sdk.model.request.JanusCallRequest
import tsa.videocall.sdk.model.request.JanusJsepRequest
import tsa.videocall.sdk.model.response.JanusEventResponse
import tsa.videocall.sdk.model.response.toSessionDescription
import tsa.videocall.sdk.plugin.JanusPlugin
import tsa.videocall.sdk.plugin.JanusPluginName
import tsa.videocall.sdk.utils.randomTransactionId
import tsa.videocall.sdk.websocket.JanusWSClient

internal class JanusEchoPlugin(janusClient: JanusWSClient) : JanusPlugin(janusClient) {

    override val plugin: JanusPluginName
        get() = JanusPluginName.ECHO

    override fun execute(command: JanusCommand) {
        super.execute(command)
        when (command) {
            is JanusCommand.Call -> {
                val request = JanusCallRequest(
                    name = JanusCommandName.MESSAGE,
                    transactionId = randomTransactionId(),
                    sessionId = sessionId,
                    handleId = handleId,
                    jsep = JanusJsepRequest(
                        command.sdp.type.canonicalForm(),
                        command.sdp.description
                    ),
                    body = JanusBodyRequest(
                        request = JanusBodyRequest.Request.CALL,
                        userId = command.userId,
                        audio = command.audio,
                        video = command.video
                    )
                )
                call(request)
            }
        }
    }

    override fun onEvent(event: JanusEventResponse) {
        val transactionId = event.transactionId
        when (event.event) {
            JanusEventName.SUCCESS -> {
                popJanusTransaction(transactionId)?.onSuccess(event)
            }
            JanusEventName.ERROR -> {
                popJanusTransaction(transactionId)?.onError(event)
            }
            JanusEventName.ACK -> {
            }
            else -> {
                if (event.hasSender && handleId == event.senderId) {
                    val description = event.toSessionDescription()
                    if (description != null) {
                        onIncomingCallEvent(handleId = handleId, userId = 132324, description)
                    }
                    val pluginData = event.pluginData
                    if (pluginData != null && plugin == pluginData.plugin) {
                        Log.e(TAG, "onEvent: ${pluginData.data}")
                    }
                }
            }
        }
    }
}