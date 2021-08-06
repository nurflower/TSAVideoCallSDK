package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tsa.videocall.sdk.model.config.JanusCommandName
import tsa.videocall.sdk.plugin.JanusPluginName

@JsonClass(generateAdapter = true)
data class JanusAttachSessionRequest(
    @Json(name = "janus") val name: JanusCommandName,
    @Json(name = "transaction") val transactionId: String,
    @Json(name = "plugin") val plugin: JanusPluginName,
    @Json(name = "session_id") val sessionId: Long
)