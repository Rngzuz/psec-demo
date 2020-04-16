package chat.mou.messaging;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public enum Action {
        MESSAGE,
        CONNECT,
        DISCONNECT
    }

    public final String displayName;
    public final Action action;
    public final String body;

    public ChatMessage(String displayName, Action action, String body) {
        this.displayName = displayName;
        this.action = action;
        this.body = body;
    }
}
