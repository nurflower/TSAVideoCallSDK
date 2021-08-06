package tsa.videocall.sdk.listener

import org.webrtc.SessionDescription
import tsa.videocall.sdk.model.config.JanusError

interface JanusCallingEventListener {

    fun onJanusIncoming(handleId:Long?, userId: Long, remoteSdp: SessionDescription)

    fun onJanusAccepted(userId: String, remoteSdp: SessionDescription)

    fun onJanusLocalAccepted(remoteSdp: SessionDescription)

    fun onJanusHangup()

    fun onJanusError(error: JanusError)

    fun onJanusJoined(privateId: Long)

    fun onNewRemoteFeed(feedId: Long)

    fun onUnpublished(handleId: Long?)

    fun onPublisherLeft(handleId: Long?)

    fun onTalking(handleId: Long?, audioLevel:Int?)

    fun onStoppedTalking(handleId: Long?)

    fun onRoomChecked(isExists: Boolean, room: Long?)

    fun onRoomCreated(room: Long?)

    fun onSubscriberConfigured(handleId: Long?, userId: Long, remoteSdp: SessionDescription)

    fun onSubscriberStarted(handleId: Long?, userId: Long?)
}