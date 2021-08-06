package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tsa.videocall.sdk.model.config.JanusCommandName

@JsonClass(generateAdapter = true)
data class JanusUnsubscribeRequest(
    @Json(name = "janus") val name: JanusCommandName,
    @Json(name = "transaction") val transactionId: String,
    @Json(name = "session_id") val sessionId: Long,
    @Json(name = "handle_id") val handleId: Long,
    @Json(name = "body") val body: JanusUnsubscribeBodyRequest
){
    @JsonClass(generateAdapter = true)
    data class JanusUnsubscribeBodyRequest(
        @Json(name = "request") val request: JanusBodyRequest.Request,
        @Json(name = "streams") val streams: List<Stream>? = null
    )

    @JsonClass(generateAdapter = true)
    data class Stream(
        @Json(name = "feed_id") val feedId: Long? = null,
        @Json(name = "mid") val mid: String? = null,
        @Json(name = "sub_mid") val subMid: String? = null
    )
}
