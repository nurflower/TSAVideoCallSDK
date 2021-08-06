package tsa.videocall.sdk.websocket


import com.tinder.scarlet.websocket.WebSocketEvent
import io.reactivex.Flowable
import tsa.videocall.sdk.model.request.*
import tsa.videocall.sdk.model.response.JanusEventResponse


interface JanusWSClient {

    val observeWebSocketEvent: Flowable<WebSocketEvent>

    val observeJanusEvent: Flowable<JanusEventResponse>

    fun initWS(wss: String)

    fun closeWS()

    fun create(request: JanusCreateSessionRequest)

    fun attach(request: JanusAttachSessionRequest)

    fun keepAlive(request: JanusKeepAliveSessionRequest)

    fun register(request: JanusRegisterRequest)

    fun call(request: JanusCallRequest)

    fun answer(request: JanusAnswerRequest)

    fun hangup(request: JanusHangupRequest)

    fun trickleCandidate(request: JanusTrickleCandidateRequest)

    fun claim(request: JanusClaimRequest)

    fun joinRoom(request: JanusJoinRoomRequest)

    fun start(request: JanusStartRequest)

    fun leave(request: JanusLeaveRequest)

    fun pause(request: JanusPauseRequest)

    fun unsubscribe(request: JanusUnsubscribeRequest)

    fun configure(request: JanusConfigureRequest)

    fun unpublish(request: JanusUnpublishRequest)

    fun publish(request: JanusPublishRequest)

    fun configureMedia(request: JanusConfigureMediaRequest)

    fun checkRoom(request: JanusCheckRoomRequest)

    fun createRoom(request: JanusCreateRoomRequest)
}