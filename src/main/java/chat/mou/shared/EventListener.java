package chat.mou.shared;

import java.util.function.Consumer;

public class EventListener
{
    private final EventType eventType;
    private final Consumer<Message> consumer;

    public EventListener(EventType eventType, Consumer<Message> consumer) {
        this.eventType = eventType;
        this.consumer = consumer;
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public Consumer<Message> getConsumer()
    {
        return consumer;
    }
}
