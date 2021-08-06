package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusPublishRequestBody(
    @Json(name = "request") val request: JanusBodyRequest.Request,
    @Json(name = "audiocodec") val audioCodec: String? = null,
    @Json(name = "videocodec") val videoCodec: String? = null,
    @Json(name = "bitrate") val bitrate: Int? = null,
    @Json(name = "record") val record: Boolean? = false,
    @Json(name = "filename") val fileName: String? = null,
    @Json(name = "display") val display: String? = null,
    @Json(name = "descriptions") val descriptions: List<Description>? = null
)
