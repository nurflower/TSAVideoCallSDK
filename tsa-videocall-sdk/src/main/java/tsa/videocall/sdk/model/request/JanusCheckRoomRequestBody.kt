package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusCheckRoomRequestBody(
    @Json(name = "request") val request: JanusBodyRequest.Request,
    @Json(name = "room") val room: Long?,
)