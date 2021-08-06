package tsa.videocall.sdk.plugin.videoroom
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.webrtc.SessionDescription
import tsa.videocall.sdk.model.config.*
import tsa.videocall.sdk.model.request.*
import tsa.videocall.sdk.model.response.JanusEventResponse
import tsa.videocall.sdk.model.response.toSessionDescription
import tsa.videocall.sdk.plugin.JanusPlugin
import tsa.videocall.sdk.plugin.JanusPluginName
import tsa.videocall.sdk.utils.randomTransactionId
import tsa.videocall.sdk.websocket.JanusWSClient
import kotlin.math.roundToLong

internal class JanusVideoRoomPlugin(janusClient: JanusWSClient, private val moshi: Moshi) : JanusPlugin(janusClient) {

    override val plugin: JanusPluginName get() = JanusPluginName.VIDEO_ZOOM

    private var privateId: Long? = null
    private var roomId: Long? = null

    override fun execute(command: JanusCommand) {
        super.execute(command)
        when (command) {
            is JanusCommand.JoinRoom -> joinRoom(command.roomId, command.displayName)

            is JanusCommand.Subscribe -> subscribe()

            is JanusCommand.Call -> call( command.sdp, command.audio, command.video)

            is JanusCommand.Answer -> answer(command.handleId ?: handleId, command.sdp)

            is JanusCommand.Unsubscribe -> unsubscribe(command.handleId, command.feedId)

            is JanusCommand.Hangup -> hangup(command.handleId)

            is JanusCommand.Start -> start(command.handleId)

            is JanusCommand.Pause -> pause(command.handleId)

            is JanusCommand.Leave -> leave(command.handleId)

            is JanusCommand.Unpublish -> unpublish(command.handleId ?: handleId)

            is JanusCommand.Publish -> publish(handleId = command.handleId ?: handleId,
                audioCodec = command.audioCodec,
                videoCodec = command.videoCodec,
                displayName = command.displayName,
                record = command.record,
                bitrate = command.bitrate,
                fileName = command.fileName)

            is JanusCommand.Configure -> configure(handleId = command.handleId ?: handleId,
                    displayName = command.displayName,
                    bitrate = command.bitrate,
                    record = command.record,
                    fileName = command.fileName)

            is JanusCommand.ConfigureMedia -> configureMedia(audio = command.audio, video = command.video)

            is JanusCommand.CheckRoom -> checkRoom(command.roomId)

            is JanusCommand.CreateRoom -> createRoom(command.roomId, bitrate = command.bitrate, bitrateCap = command.bitrateCap, record = command.record, recDir = command.recDir)

            else -> {

            }
        }
    }

    override fun onEvent(event: JanusEventResponse) {
        val transactionId = event.transactionId
        when (event.event) {
            JanusEventName.SUCCESS -> {
                popJanusTransaction(transactionId)?.onSuccess(event)
                val pluginData = parsePluginData(event.pluginData)
                if (pluginData?.videoRoom != null) onResultEvent(event)
            }
            JanusEventName.ERROR -> {
                val janusTransactionListener = popJanusTransaction(transactionId)
                if (janusTransactionListener != null) {
                    janusTransactionListener.onError(event)
                } else if (event.error != null) {
                    val error = event.error.error ?: return
                    onErrorEvent(error)
                }
            }
            JanusEventName.ACK -> {

            }
            else -> {
                if (event.hasSender) {
                    when (event.event) {
                        JanusEventName.EVENT -> onResultEvent(event)
                        JanusEventName.DETACHED -> onLeaving()
                    }
                }
            }
        }
    }

    private fun onResultEvent(event: JanusEventResponse) {
        val description = event.toSessionDescription()
        val pluginData = parsePluginData(event.pluginData)
        when (pluginData?.videoRoom) {

            JanusPluginDataEvent.SUCCESS -> {
                if (pluginData.exists != null){
                    onRoomChecked(pluginData.exists, pluginData.room)
                }
            }
            JanusPluginDataEvent.CREATED -> {
                onRoomCreated(pluginData.room)
            }

            JanusPluginDataEvent.JOINED -> {
                privateId = pluginData.privateId
                roomId = pluginData.room
                privateId?.let { onJoined(it, pluginData.id) }
                if (pluginData.publishers != null && pluginData.publishers.isNotEmpty()){
                    pluginData.publishers.forEach {
                        onNewRemoteFeed(it.id)
                    }
                }
            }
            JanusPluginDataEvent.TALKING -> {
                onTalking(pluginData.id, pluginData.audioLevel?.toInt())
            }

            JanusPluginDataEvent.STOPPED_TALKING -> {
                onStoppedTalking(pluginData.id)
            }

            JanusPluginDataEvent.SLOW_LINK -> {

            }

            JanusPluginDataEvent.EVENT -> {

                if (pluginData.configured != null && pluginData.configured == "ok"){
                    if (description != null){
                        if (event.senderId == handleId){
                            onAcceptedLocalCallEvent(description)
                        }else {
                            val userId = subscriberHandleIds.filterValues { it == event.senderId }.keys.elementAt(0)
                            onSubscriberConfigured(event.senderId, userId, description)
                        }
                    }
                }

                if (pluginData.started != null && pluginData.started == "ok"){
                    val userId = subscriberHandleIds.filterValues { it == event.senderId }.keys.elementAt(0)
                    onSubscriberStarted(event.senderId, userId)
                }

                if(pluginData.unpublished != null && pluginData.unpublished == "ok"){
                    onUnpublished(handleId)
                }

                if(pluginData.unpublished != null && pluginData.unpublished != "ok"){
                    onUnpublished(pluginData.unpublished.toDouble().roundToLong())
                }

                if (pluginData.leaving != null){
                    onPublisherLeft(pluginData.leaving)
                }

                if (pluginData.publishers != null && pluginData.publishers.isNotEmpty()){
                    pluginData.publishers.forEach {
                        onNewRemoteFeed(it.id)
                    }
                }
            }

            JanusPluginDataEvent.ATTACHED -> {
                if (description != null){
                    val userId = subscriberHandleIds.filterValues { it == event.senderId }.keys.elementAt(0)
                    onIncomingCallEvent(event.senderId, userId, description)
                }
            }

            else -> {
                val error = pluginData?.errorCode
                if (error != null) {
                    onErrorEvent(error)
                }
            }
        }

    }

    private fun onErrorEvent(errorCode: Int) {
        val error = JanusError.get(errorCode) ?: return
        onError(error)
    }


    private fun checkRoom(roomId: Long?){
        val request = JanusCheckRoomRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusCheckRoomRequestBody(
                room = roomId,
                request = JanusBodyRequest.Request.EXISTS
            )
        )
        checkRoom(request)
    }

    private fun createRoom(roomId: Long?, bitrate: Long? = 512000, bitrateCap: Boolean = true, record: Boolean? = false, recDir: String? = null){
        val request = JanusCreateRoomRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusCreateRoomRequestBody(
                room = roomId,
                request = JanusBodyRequest.Request.CREATE,
                bitrate = bitrate,
                bitrateCap = bitrateCap,
                firFreq = 30,
                record = record,
                recDir = recDir
            )
        )
        createRoom(request)
    }


    private fun subscribe(){
        subscriberHandleIds.entries.last()

        val request = JanusJoinRoomRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = subscriberHandleIds.entries.last().value,
            body = JanusJoinRoomBodyRequest(
                room = roomId,
                ptype = JanusJoinRoomBodyRequest.PType.SUBSCRIBER,
                privateId = privateId,
                feed = subscriberHandleIds.entries.last().key
            )
        )
        joinRoom(request)
    }


    private fun unsubscribe(handleId: Long, feedId: Long?){
        val request = JanusUnsubscribeRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId =  handleId,
            body =  JanusUnsubscribeRequest.JanusUnsubscribeBodyRequest(
                request = JanusBodyRequest.Request.UNSUBSCRIBE,
                streams = listOf(JanusUnsubscribeRequest.Stream(feedId = feedId))
            )
        )
        unsubscribe(request)
    }


    //offer to call
    private fun call(sdp: SessionDescription, audio: Boolean = true, video: Boolean = true) {
        val request = JanusCallRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            jsep = JanusJsepRequest(
                sdp.type.canonicalForm(),
                sdp.description
            ),
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.CONFIGURE,
                audio = audio,
                video = video
            )
        )
        call(request)
    }

    private fun configureMedia(audio: Boolean?, video: Boolean?){
        val request = JanusConfigureMediaRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.CONFIGURE,
                audio = audio,
                video = video
            )
        )
        configureMedia(request)
    }

    //create answer
    private fun answer(handleId: Long, sdp: SessionDescription) {
        val request = JanusAnswerRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            jsep = JanusJsepRequest(
                sdp.type.canonicalForm(),
                sdp.description
            ),
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.START,
                room = roomId
            )
        )
        answer(request)
    }


    private fun hangup(handleId: Long) {
        val request = JanusHangupRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.HANGUP
            )
        )
        hangup(request)
    }


    private fun joinRoom(roomId: Long, displayName: String){
        val request = JanusJoinRoomRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusJoinRoomBodyRequest(
                room = roomId,
                ptype = JanusJoinRoomBodyRequest.PType.PUBLISHER,
                display = displayName
            )
        )
        joinRoom(request)
    }

    private fun pause(handleId: Long){
        val request = JanusPauseRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.PAUSE,
                room = roomId
            )
        )
        pause(request)
    }

    private fun start(handleId: Long){
        val request = JanusStartRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.START
            )
        )
        start(request)
    }

    private fun leave(handleId: Long){
        val request = JanusLeaveRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.LEAVE,
                room = roomId
            )
        )
        leave(request)
    }

    private fun configure(handleId: Long, displayName: String? = null, bitrate: Int? = null, record: Boolean? = false, fileName: String? = "/opt/janus/share/janus/recordings"){
        val request = JanusConfigureRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusConfigureBodyRequest(
                request = JanusBodyRequest.Request.CONFIGURE,
                bitrate = bitrate,
                display = displayName,
                record = record,
                fileName = fileName
            )
        )
        configure(request)
    }

    private fun publish(handleId: Long, audioCodec: String? = null, videoCodec: String? = null, bitrate: Int? = null, record: Boolean? = false, fileName: String? = null, displayName: String? = null){
        val request = JanusPublishRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusPublishRequestBody(
                request = JanusBodyRequest.Request.PUBLISH,
                bitrate = bitrate,
                display = displayName,
                record = record,
                fileName = fileName,
                audioCodec = audioCodec,
                videoCodec = videoCodec
            )
        )
        publish(request)
    }

    private fun unpublish(handleId: Long){
        val request = JanusUnpublishRequest(
            name = JanusCommandName.MESSAGE,
            transactionId = randomTransactionId(),
            sessionId = sessionId,
            handleId = handleId,
            body = JanusBodyRequest(
                request = JanusBodyRequest.Request.UNPUBLISH
            )
        )
        unpublish(request)
    }


    private fun parsePluginData(pluginData: JanusEventResponse.PluginData?): JanusVideoRoomPluginData? {
        return if (pluginData != null && plugin == pluginData.plugin && pluginData.data != null) {
            val map = pluginData.data as? Map<*, *>
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<*, *>>(type)
            val json = adapter.toJson(map)
            moshi.adapter(JanusVideoRoomPluginData::class.java).fromJson(json)
        } else null
    }
}