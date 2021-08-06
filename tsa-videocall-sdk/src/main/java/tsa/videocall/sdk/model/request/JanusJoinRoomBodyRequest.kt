package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusJoinRoomBodyRequest(
    @Json(name = "room") val room: Long?,
    @Json(name = "ptype") val ptype: PType,
    @Json(name = "request") val request: String = "join",
    @Json(name = "display") val display: String? = null,
    @Json(name = "private_id") val privateId: Long? = null,
    @Json(name = "feed") val feed: Long? = null
){
    enum class PType{
        SUBSCRIBER{
            override val value: String
                get() = "subscriber"
        },
        PUBLISHER{
            override val value: String
                get() = "publisher"
        };
        abstract val value: String
    }
}
