package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tsa.videocall.sdk.model.config.JanusCommandName


@JsonClass(generateAdapter = true)
data class JanusTrickleCandidateRequest(
    @Json(name = "janus") val name: JanusCommandName,
    @Json(name = "transaction") val transactionId: String,
    @Json(name = "session_id") val sessionId: Long,
    @Json(name = "handle_id") val handleId: Long,
    @Json(name = "candidate") val candidate: Candidate
) {

    @JsonClass(generateAdapter = true)
    data class Candidate(
        @Json(name = "candidate") val candidate: String? = null,
        @Json(name = "sdpMid") val sdpMid: String? = null,
        @Json(name = "sdpMLineIndex") val sdpMLineIndex: Int? = null,
        @Json(name = "completed") val completed: Boolean = false
    )
}