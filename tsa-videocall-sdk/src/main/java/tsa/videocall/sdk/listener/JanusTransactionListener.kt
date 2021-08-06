package tsa.videocall.sdk.listener

import tsa.videocall.sdk.model.response.JanusEventResponse


interface JanusTransactionListener {

    fun onSuccess(response: JanusEventResponse)

    fun onError(response: JanusEventResponse) {

    }
}