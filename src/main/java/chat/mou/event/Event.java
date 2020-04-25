package chat.mou.event;

import chat.mou.event.message.TextMessage;

import java.io.Serializable;

public enum Event
{
    BROADCAST(TextMessage.class);

    private final Class<? extends Serializable> type;

    Event(Class<? extends Serializable> type)
    {
        this.type = type;
    }

    public Class<? extends Serializable> getType()
    {
        return type;
    }
}
