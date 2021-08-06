package tsa.videocall.sdk.websocket
import com.tinder.scarlet.websocket.WebSocketEvent
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import tsa.videocall.sdk.model.request.*
import tsa.videocall.sdk.model.response.JanusEventResponse

internal interface JanusWSService {

    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocketEvent>

    @Receive
    fun observeJanusEvent(): Flowable<JanusEventResponse>

    @Send
    fun createSession(request: JanusCreateSessionRequest): Boolean

    @Send
    fun attachSession(request: JanusAttachSessionRequest): Boolean

    @Send
    fun register(request: JanusRegisterRequest): Boolean

    @Send
    fun keepAliveSession(request: JanusKeepAliveSessionRequest): Boolean

    @Send
    fun call(request: JanusCallRequest): Boolean

    @Send
    fun answer(request: JanusAnswerRequest): Boolean

    @Send
    fun hangup(request: JanusHangupRequest): Boolean

    @Send
    fun trickleCandidate(request: JanusTrickleCandidateRequest): Boolean

    @Send
    fun claim(request: JanusClaimRequest): Boolean

    @Send
    fun joinRoom(request: JanusJoinRoomRequest): Boolean

     @Send
     fun start(request: JanusStartRequest): Boolean

     @Send
     fun leave(request: JanusLeaveRequest): Boolean

     @Send
     fun pause(request: JanusPauseRequest): Boolean

     @Send
     fun unsubscribe(request: JanusUnsubscribeRequest): Boolean

     @Send
     fun configure(request: JanusConfigureRequest): Boolean

     @Send
     fun unpublish(request: JanusUnpublishRequest): Boolean

     @Send
     fun publish(request: JanusPublishRequest) : Boolean

     @Send
     fun configureMedia(request: JanusConfigureMediaRequest): Boolean

     @Send
     fun checkRoom(request: JanusCheckRoomRequest): Boolean

     @Send
     fun createRoom(request: JanusCreateRoomRequest):Boolean

}