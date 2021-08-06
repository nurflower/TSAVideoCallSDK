package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Description(
    @Json(name = "mid") val mid: String,
    @Json(name = "description") val description: String
)