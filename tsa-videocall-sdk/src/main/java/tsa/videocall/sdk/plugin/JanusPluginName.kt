package tsa.videocall.sdk.plugin

enum class JanusPluginName {

    ECHO {
        override val plugin: String
            get() = "janus.plugin.echotest"
    },

    VIDEO_CALL {
        override val plugin: String
            get() = "janus.plugin.videocall"
    },

    VIDEO_ZOOM {
        override val plugin: String
            get() = "janus.plugin.videoroom"
    };

    abstract val plugin: String
}