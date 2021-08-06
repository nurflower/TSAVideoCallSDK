package tsa.videocall.sdk.model.config


enum class JanusCommandName {

    CREATE {
        override val command: String
            get() = "create"
    },
    ATTACH {
        override val command: String
            get() = "attach"
    },
    TRICKLE {
        override val command: String
            get() = "trickle"
    },
    DESTROY {
        override val command: String
            get() = "destroy"
    },
    DETACH {
        override val command: String
            get() = "detach"
    },
    KEEP_ALIVE {
        override val command: String
            get() = "keepalive"
    },
    MESSAGE {
        override val command: String
            get() = "message"
    },
    HANGUP {
        override val command: String
            get() = "hangup"
    },
    CLAIM {
        override val command: String
            get() = "claim"
    },
    JOIN {
        override val command: String
            get() = "join"
    };

    abstract val command: String
}