package tsa.videocall.sdk.websocket

import android.content.Context

import com.squareup.moshi.Moshi
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.ShutdownReason
import com.tinder.scarlet.websocket.WebSocketEvent
import com.tinder.scarlet.websocket.okhttp.OkHttpWebSocket
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.Request
import tsa.videocall.sdk.model.request.*
import tsa.videocall.sdk.model.response.JanusEventResponse
import tsa.videocall.sdk.websocket.lifecycle.CallSessionOnLifecycle
import tsa.videocall.sdk.websocket.lifecycle.ConnectivityOnLifecycle


internal class JanusWSClientImpl(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : JanusWSClient {

    override val observeWebSocketEvent: Flowable<WebSocketEvent>
        get() = janusWSService?.observeWebSocketEvent() ?: throw ExceptionInInitializerError()

    override val observeJanusEvent: Flowable<JanusEventResponse>
        get() = janusWSService?.observeJanusEvent() ?: throw ExceptionInInitializerError()

    private var scarlet: Scarlet? = null
    private var janusWSService: JanusWSService? = null
    private var callSessionOnLifecycle: CallSessionOnLifecycle? = null
    private var connectivityOnLifecycle: ConnectivityOnLifecycle? = null

    override fun initWS(wss: String) {
        val callSessionOnLifecycle = CallSessionOnLifecycle()
            .also { callSessionOnLifecycle = it }

        val connectivityOnLifecycle = ConnectivityOnLifecycle(context)
            .also { connectivityOnLifecycle = it }

        val config = Scarlet.Configuration(
            lifecycle = callSessionOnLifecycle.combineWith(connectivityOnLifecycle),
            messageAdapterFactories = listOf(MoshiMessageAdapter.Factory(moshi = moshi)),
            streamAdapterFactories = listOf(RxJava2StreamAdapterFactory())
        )

        val scarlet = Scarlet(
            OkHttpWebSocket(
                okHttpClient,
                OkHttpWebSocket.SimpleRequestFactory({
                    Request.Builder()
                        .url(wss)
                        .header("Sec-WebSocket-Protocol", "janus-protocol")
                        .build()
                }, { ShutdownReason.GRACEFUL })
            ),
            config
        ).also { scarlet = it }

        this.janusWSService = scarlet.create()
        callSessionOnLifecycle.start()
    }

    override fun closeWS() {
        callSessionOnLifecycle?.end()
        this.scarlet = null
        this.janusWSService = null
        this.callSessionOnLifecycle = null
        this.connectivityOnLifecycle?.close()
        this.connectivityOnLifecycle = null
    }

    override fun create(request: JanusCreateSessionRequest) {
        janusWSService?.createSession(request)
    }

    override fun attach(request: JanusAttachSessionRequest) {
        janusWSService?.attachSession(request)
    }

    override fun keepAlive(request: JanusKeepAliveSessionRequest) {
        janusWSService?.keepAliveSession(request)
    }

    override fun register(request: JanusRegisterRequest) {
        janusWSService?.register(request)
    }

    override fun call(request: JanusCallRequest) {
        janusWSService?.call(request)
    }

    override fun answer(request: JanusAnswerRequest) {
        janusWSService?.answer(request)
    }

    override fun hangup(request: JanusHangupRequest) {
        janusWSService?.hangup(request)
    }

    override fun trickleCandidate(request: JanusTrickleCandidateRequest) {
        janusWSService?.trickleCandidate(request)
    }

    override fun claim(request: JanusClaimRequest) {
        janusWSService?.claim(request)
    }

    override fun joinRoom(request: JanusJoinRoomRequest) {
        janusWSService?.joinRoom(request)
    }

    override fun start(request: JanusStartRequest) {
        janusWSService?.start(request)
    }

    override fun leave(request: JanusLeaveRequest) {
        janusWSService?.leave(request)
    }

    override fun pause(request: JanusPauseRequest) {
        janusWSService?.pause(request)
    }

    override fun unsubscribe(request: JanusUnsubscribeRequest) {
        janusWSService?.unsubscribe(request)
    }

    override fun configure(request: JanusConfigureRequest) {
        janusWSService?.configure(request)
    }

    override fun unpublish(request: JanusUnpublishRequest) {
        janusWSService?.unpublish(request)
    }

    override fun publish(request: JanusPublishRequest) {
        janusWSService?.publish(request)
    }

    override fun configureMedia(request: JanusConfigureMediaRequest) {
        janusWSService?.configureMedia(request)
    }

    override fun checkRoom(request: JanusCheckRoomRequest) {
        janusWSService?.checkRoom(request)
    }

    override fun createRoom(request: JanusCreateRoomRequest) {
        janusWSService?.createRoom(request)
    }

}