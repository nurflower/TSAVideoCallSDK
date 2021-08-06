package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class JanusJsepRequest(
    @Json(name = "type") val type: String,
    @Json(name = "sdp") val sdp: String
)