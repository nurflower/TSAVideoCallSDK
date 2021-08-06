package tsa.videocall.sdk.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JanusBodyRequest(
    @Json(name = "request") val request: Request,
    @Json(name = "username") val userId: String? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "sid") val sid: String? = null,
    @Json(name = "video") val video: Boolean? = false,
    @Json(name = "audio") val audio: Boolean? = false,
    @Json(name = "room") val room: Long? = null
) {
    enum class Request {
        CALL {
            override val value: String
                get() = "call"
        },

        ACCEPT {
            override val value: String
                get() = "accept"
        },

        HANGUP {
            override val value: String
                get() = "hangup"
        },

        REGISTER {
            override val value: String
                get() = "register"
        },
        CONFIGURE {
            override val value: String
                get() = "configure"
        },
        START {
            override val value: String
                get() = "start"
        },
        PAUSE {
            override val value: String
                get() = "pause"
        },
        PUBLISH{
            override val value: String
                get() = "publish"
        },
        UNPUBLISH {
            override val value: String
                get() = "unpublish"
        },
        LEAVE {
            override val value: String
                get() = "leave"
        },
        UNSUBSCRIBE{
            override val value: String
                get() = "unsubscribe"
        },
        EXISTS{
            override val value: String
                get() = "exists"
        },
        CREATE{
            override val value: String
                get() = "create"
        };

        abstract val value: String
    }
}