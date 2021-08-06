package tsa.videocall.sdk.plugin.videoroom

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tsa.videocall.sdk.model.config.JanusPluginDataEvent

internal data class JanusVideoRoomPluginData (

    @Json(name = "videoroom") val videoRoom: JanusPluginDataEvent?,
    @Json(name = "room") val room: Long?,
    @Json(name = "permanent") val permanent: Boolean?,
    @Json(name = "exists") val exists: Boolean?,
    @Json(name = "allowed") val allowed: List<String>?,
    @Json(name = "participants") val participants: List<Participant>?,
    @Json(name = "publishers") val publishers: List<Publisher>?,
    @Json(name = "configured") val configured: String?,
    @Json(name = "started") val started: String?,
    @Json(name = "private_id") val privateId: Long?,
    @Json(name = "display") val name: String?,
    @Json(name = "unpublished") val unpublished: String?,
    @Json(name = "leaving") val leaving: Long?,
    @Json(name = "audio-level-dBov-avg") val audioLevel: Double?,
    @Json(name = "id") val id: Long?,
    @Json(name = "error_code") val errorCode: Int?,
    @Json(name = "error") val error: String?


) {


    @JsonClass(generateAdapter = true)
    data class Participant(
        @Json(name = "id") val id: Long?,
        @Json(name = "display") val name: String?,
        @Json(name = "publisher") val publisher: Boolean?,
        @Json(name = "talking") val talking: Boolean?
    )

    @JsonClass(generateAdapter = true)
    data class Publisher(
        @Json(name = "id") val id: Long,
        @Json(name = "display") val name: String?,
        @Json(name = "audio_codec") val audioCodec: String?,
        @Json(name = "video_codec") val videoCodec: String?,
        @Json(name = "talking") val talking: Boolean?
    )
}