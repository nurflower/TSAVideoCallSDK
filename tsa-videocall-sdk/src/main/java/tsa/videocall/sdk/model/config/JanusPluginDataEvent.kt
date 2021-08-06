package tsa.videocall.sdk.model.config

enum class JanusPluginDataEvent {
    SUCCESS{
        override val value: String
            get() = "success"
    },

    EVENT {
        override val value: String
            get() = "event"
    },

    REGISTERED {
        override val value: String
            get() = "registered"
    },

    ACCEPTED {
        override val value: String
            get() = "accepted"
    },

    HANGUP {
        override val value: String
            get() = "hangup"
    },

    INCOMING_CALL {
        override val value: String
            get() = "incomingcall"
    },

    JOINED {
        override val value: String
            get() = "joined"
    },

    LEAVING {
        override val value: String
            get() = "leaving"
    },
    ATTACHED{
        override val value: String
            get() = "attached"
    },
    SLOW_LINK {
        override val value: String
            get() = "slow_link"
    },
    TALKING {
        override val value: String
            get() = "talking"
    },
    STOPPED_TALKING {
        override val value: String
            get() = "stopped-talking"
    },
    CREATED {
        override val value: String
            get() = "created"
    };

    abstract val value: String
}