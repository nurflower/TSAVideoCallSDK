package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tsa.videocall.sdk.model.config.JanusCommandName


@JsonClass(generateAdapter = true)
data class JanusClaimRequest(
    @Json(name = "janus") val name: JanusCommandName,
    @Json(name = "transaction") val transactionId: String,
    @Json(name = "session_id") val sessionId: Long
)