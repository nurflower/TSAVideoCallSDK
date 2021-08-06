package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusCreateRoomRequestBody (
    @Json(name = "request") val request: JanusBodyRequest.Request,
    @Json(name = "room") val room: Long?,
    @Json(name = "bitrate") val bitrate: Long?,
    @Json(name =  "bitrate_cap") val bitrateCap: Boolean,
    @Json(name = "fir_freq") val firFreq: Int?,
    @Json(name = "record") val record: Boolean?,
    @Json(name = "rec_dir") val recDir: String?)