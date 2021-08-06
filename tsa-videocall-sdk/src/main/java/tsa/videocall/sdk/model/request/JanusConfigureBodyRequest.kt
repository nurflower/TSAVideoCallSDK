package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusConfigureBodyRequest(
    @Json(name = "request") val request: JanusBodyRequest.Request,
    @Json(name = "bitrate") val bitrate: Int? = null,
    @Json(name = "keyframe") val keyFrame: Boolean = false,
    @Json(name = "record") val record: Boolean? = false,
    @Json(name = "filename") val fileName: String? = null,
    @Json(name = "display") val display: String? = null,
    @Json(name = "audio_active_packets") val audioActivePackets: Int? = null,
    @Json(name = "audio_level_average") val audioLevelAverage: Int? = null,
    @Json(name = "mid") val mid: Int? = null,
    @Json(name = "send") val send: Boolean? = null,
    @Json(name = "descriptions") val descriptions: List<Description>? = null
)