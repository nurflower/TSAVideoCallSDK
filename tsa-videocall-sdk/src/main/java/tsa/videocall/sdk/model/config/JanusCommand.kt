package tsa.videocall.sdk.model.config

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

sealed class JanusCommand {
    object Create : JanusCommand()

    object Attach : JanusCommand()

    data class AttachSubscriber(val feedId: Long): JanusCommand()

    data class Call(
        val sdp: SessionDescription,
        val audio: Boolean = true,
        val video: Boolean = true,
        val userId: String? = null
    ) : JanusCommand()

    data class JoinRoom(val roomId: Long, val displayName: String) : JanusCommand()

    object Subscribe: JanusCommand()

    data class Unsubscribe(val handleId: Long, val feedId: Long?): JanusCommand()

    data class Trickle(val handleId: Long? = null, val ice: IceCandidate) : JanusCommand()

    data class Answer(val handleId: Long?, val sdp: SessionDescription) : JanusCommand()

    object Claim : JanusCommand()

    data class TrickleComplete(val handleId: Long? = null) : JanusCommand()

    data class Hangup(val handleId: Long): JanusCommand()

    data class Start(val handleId: Long): JanusCommand()

    data class Pause(val handleId: Long): JanusCommand()

    data class Leave(val handleId: Long): JanusCommand()

    data class Unpublish(val handleId: Long? = null): JanusCommand()

    data class Configure(val handleId: Long? = null, val displayName: String? = null, val bitrate: Int? = null, val record: Boolean? = false, val fileName: String? = null): JanusCommand()

    data class Publish(val handleId: Long? = null, val audioCodec: String? = null, val videoCodec: String? = null, val bitrate: Int? = null, val record: Boolean? = false, val fileName: String? = null, val displayName: String? = null): JanusCommand()

    data class ConfigureMedia(val audio: Boolean? = null, val video: Boolean? = null): JanusCommand()

    data class CheckRoom(val roomId: Long?): JanusCommand()

    data class CreateRoom(val roomId: Long?, val bitrate: Long? = 512000, val bitrateCap: Boolean = true, val record: Boolean? = false, val recDir: String? = null): JanusCommand()

}