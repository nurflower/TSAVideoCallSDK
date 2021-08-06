package tsa.videocall.sdk.plugin

import android.util.Log
import com.tinder.scarlet.websocket.WebSocketEvent
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.webrtc.SessionDescription
import tsa.videocall.sdk.listener.JanusCallingEventListener
import tsa.videocall.sdk.listener.JanusTransactionListener
import tsa.videocall.sdk.listener.OnJanusListener
import tsa.videocall.sdk.model.config.JanusCommand
import tsa.videocall.sdk.model.config.JanusCommandName
import tsa.videocall.sdk.model.config.JanusError
import tsa.videocall.sdk.model.config.JanusState
import tsa.videocall.sdk.model.request.*
import tsa.videocall.sdk.model.response.JanusEventResponse
import tsa.videocall.sdk.utils.randomTransactionId
import tsa.videocall.sdk.websocket.JanusWSClient
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.timerTask

abstract class JanusPlugin(janusClient: JanusWSClient) : JanusWSClient by janusClient {

    abstract val plugin: JanusPluginName

    private val transactions = ConcurrentHashMap<String, JanusTransactionListener>()

    val sessionId: Long get() = _sessionId
    private var _sessionId: Long = -1

    val handleId: Long get() = _handleId
    private var _handleId: Long = -1

    val publisherId: Long? get() = _publisherId
    private var _publisherId: Long? = -1

    val subscriberHandleIds: Map<Long, Long> get() = _subscriberHandleIds
    private var _subscriberHandleIds = LinkedHashMap<Long, Long>()

    var janusCallingEventListener: JanusCallingEventListener? = null

    var onJanusListener: OnJanusListener? = null
        set(value) {
            field = value
            field?.onJanusStateChanged(plugin, janusReadyState)
        }

    private var janusReadyState: JanusState = JanusState.CLOSED
        set(value) {
            field = value
            onJanusListener?.onJanusStateChanged(plugin, field)
        }

    private var observeWSEventDisposable: Disposable? = null
    private var observeJanusEventWSDisposable: Disposable? = null

    private var keepAliveTimer: Timer? = null
    private var keepAliveTimerTask: TimerTask? = null

    abstract fun onEvent(event: JanusEventResponse)

    open fun execute(command: JanusCommand) {
        when (command) {
            is JanusCommand.Create -> {
                val transitionId = randomTransactionId()
                val request = JanusCreateSessionRequest(
                    name = JanusCommandName.CREATE,
                    transactionId = transitionId
                )

                janusReadyState = JanusState.INIT

                transactions[transitionId] = object : JanusTransactionListener {
                    override fun onSuccess(response: JanusEventResponse) {
                        val sessionId = response.data?.id
                        if (sessionId != null) {
                            _sessionId = sessionId
                            execute(JanusCommand.Attach)
                        }
                    }
                }
                create(request)
            }

            is JanusCommand.Attach -> {
                stopKeepAlive()
                scheduleKeepAlive()

                val transitionId = randomTransactionId()
                val request = JanusAttachSessionRequest(
                    name = JanusCommandName.ATTACH,
                    transactionId = transitionId,
                    plugin = plugin,
                    sessionId = sessionId
                )

                transactions[transitionId] = object : JanusTransactionListener {
                    override fun onSuccess(response: JanusEventResponse) {
                        val handleId = response.data?.id
                        if (handleId != null) {
                            _handleId = handleId

                            janusReadyState = JanusState.READY
                        }
                    }
                }
                attach(request)
            }

            is JanusCommand.AttachSubscriber ->{
                stopKeepAlive()
                scheduleKeepAlive()

                val transitionId = randomTransactionId()
                val request = JanusAttachSessionRequest(
                    name = JanusCommandName.ATTACH,
                    transactionId = transitionId,
                    plugin = plugin,
                    sessionId = sessionId
                )

                transactions[transitionId] = object : JanusTransactionListener {
                    override fun onSuccess(response: JanusEventResponse) {
                        val handleId = response.data?.id
                        if (handleId != null) {
                            _subscriberHandleIds[command.feedId] = handleId
                            janusReadyState = JanusState.ATTACHED
                        }
                    }
                }
                attach(request)
            }

            is JanusCommand.Claim -> {
                val request = JanusClaimRequest(
                    name = JanusCommandName.CLAIM,
                    transactionId = randomTransactionId(),
                    sessionId = sessionId
                )
                claim(request)
            }

            is JanusCommand.Trickle -> {
                val request = JanusTrickleCandidateRequest(
                    name = JanusCommandName.TRICKLE,
                    transactionId = randomTransactionId(),
                    sessionId = sessionId,
                    handleId = command.handleId ?: handleId,
                    candidate = JanusTrickleCandidateRequest.Candidate(
                        command.ice.sdp,
                        command.ice.sdpMid,
                        command.ice.sdpMLineIndex
                    )
                )
                trickleCandidate(request)
            }

            is JanusCommand.TrickleComplete -> {
                val request = JanusTrickleCandidateRequest(
                    name = JanusCommandName.TRICKLE,
                    transactionId = randomTransactionId(),
                    sessionId = sessionId,
                    handleId = command.handleId ?: handleId,
                    candidate = JanusTrickleCandidateRequest.Candidate(completed = true)
                )
                trickleCandidate(request)
            }

        }
    }

    open fun close() {
        _sessionId = -1
        _handleId = -1
        _publisherId = -1
        _subscriberHandleIds.clear()
        transactions.clear()
        stopKeepAlive()
        observeWSEventDisposable?.dispose()
        observeJanusEventWSDisposable?.dispose()
        closeWS()
        janusReadyState = JanusState.CLOSED
        onJanusListener?.onJanusConnectionChanged(false)
    }

    fun observeWebSocket() {
        observeWSEventDisposable = observeWebSocketEvent
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG, "observeWebSocket: $it")
                when (it) {
                    is WebSocketEvent.OnConnectionOpened -> {
                        onJanusListener?.onJanusConnectionChanged(true)
                        when (janusReadyState) {
                            JanusState.CLOSED -> execute(JanusCommand.Create)
                            JanusState.INIT -> execute(JanusCommand.Attach)
                            else -> {}
                        }
                    }
                    is WebSocketEvent.OnConnectionClosing, is WebSocketEvent.OnConnectionClosed, is WebSocketEvent.OnConnectionFailed -> {
                        onJanusListener?.onJanusConnectionChanged(false)
                    }

                    else -> {

                    }
                }
            }, {
                Log.e(TAG, "observeWebSocket: ", it)
            })

        observeJanusEventWSDisposable = observeJanusEvent
            .subscribeOn(Schedulers.io())
            .subscribe({ janus ->
                onEvent(janus)
            }, {
                Log.e(TAG, "observeWebSocket: ", it)
            })
    }

    fun popJanusTransaction(transactionId: String?) = transactions.remove(transactionId)

    private fun scheduleKeepAlive() {
        keepAliveTimer = Timer(KEEP_ALIVE_SESSION_TIMER_NAME)
        keepAliveTimerTask = timerTask {
            val request = JanusKeepAliveSessionRequest(
                name = JanusCommandName.KEEP_ALIVE,
                transactionId = randomTransactionId(),
                sessionId = sessionId
            )
            keepAlive(request)
        }
        keepAliveTimer?.schedule(
            keepAliveTimerTask,
            KEEP_ALIVE_SESSION_TIMER_DELAY,
            KEEP_ALIVE_SESSION_TIMER_PERIOD
        )
    }

    private fun stopKeepAlive() {
        keepAliveTimerTask?.cancel()
        keepAliveTimerTask = null
        keepAliveTimer?.cancel()
        keepAliveTimer = null
    }

    protected fun onIncomingCallEvent(handleId: Long?, userId: Long, remoteSdp: SessionDescription) {
        janusCallingEventListener?.onJanusIncoming(handleId, userId, remoteSdp)
    }

    protected fun onAcceptedLocalCallEvent(remoteSdp: SessionDescription){
        janusCallingEventListener?.onJanusLocalAccepted(remoteSdp)
    }

    protected fun onAcceptedCallEvent(userId: String, remoteSdp: SessionDescription) {
        janusCallingEventListener?.onJanusAccepted(userId, remoteSdp)
    }

    protected fun onHangup() {
        janusCallingEventListener?.onJanusHangup()
    }

    protected fun onLeaving() {
    }

    protected fun onError(error: JanusError) {
        janusCallingEventListener?.onJanusError(error)
    }

    protected fun onJoined(privateId: Long, publisherId: Long?){
        _publisherId = publisherId
        janusCallingEventListener?.onJanusJoined(privateId)
    }

    protected fun onNewRemoteFeed(feedId: Long){
        janusCallingEventListener?.onNewRemoteFeed(feedId)
    }

    protected fun onUnpublished(handleId: Long?){
        _subscriberHandleIds.remove(handleId)
        janusCallingEventListener?.onUnpublished(handleId)
    }

    protected fun onPublisherLeft(handleId: Long?){
        _subscriberHandleIds.remove(handleId)
        janusCallingEventListener?.onPublisherLeft(handleId)
    }

    protected fun onTalking(feedId: Long?, audioLevel: Int?){
        janusCallingEventListener?.onTalking(feedId, audioLevel)
    }

    protected fun onStoppedTalking(handleId: Long?){
        janusCallingEventListener?.onStoppedTalking(handleId)
    }

    protected fun onRoomChecked(isExists: Boolean, room: Long?){
        janusCallingEventListener?.onRoomChecked(isExists, room)
    }

    protected fun onRoomCreated(room: Long?){
        janusCallingEventListener?.onRoomCreated(room)
    }

    protected fun onSubscriberConfigured(handleId: Long?, userId: Long, remoteSdp: SessionDescription){
        janusCallingEventListener?.onSubscriberConfigured(handleId, userId, remoteSdp)
    }

    protected fun onSubscriberStarted(handleId: Long?, userId: Long){
        janusCallingEventListener?.onSubscriberStarted(handleId, userId)
    }

    companion object {
        internal val TAG: String = JanusPlugin::class.java.simpleName
        private const val KEEP_ALIVE_SESSION_TIMER_NAME = "KeepAliveSession"
        private const val KEEP_ALIVE_SESSION_TIMER_DELAY = 5_000L
        private const val KEEP_ALIVE_SESSION_TIMER_PERIOD = 5_000L
    }
}