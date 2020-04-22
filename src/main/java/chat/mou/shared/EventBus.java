package chat.mou.shared;

import java.util.*;
import java.util.function.Consumer;

// Question on concurrent list/map
// https://stackoverflow.com/questions/21257104/enummap-with-concurrent-put-get
public class EventBus
{
    private final List<EventListener> eventListeners;

    public EventBus()
    {
        this.eventListeners = Collections.synchronizedList(new ArrayList<>());
    }

    public EventListenerToken addListener(EventType eventType, Consumer<Message> consumer)
    {
        final var eventListener = new EventListener(eventType, consumer);
        eventListeners.add(eventListener);

        return new EventListenerToken()
        {
            @Override
            public int getIndex()
            {
                return eventListeners.indexOf(eventListener);
            }

            @Override
            public EventListener getListener()
            {
                return eventListener;
            }

            @Override
            public boolean removeListener()
            {
                return eventListeners.remove(eventListener);
            }
        };
    }

    public void clear()
    {
        eventListeners.clear();
    }

    public void dispatch(Message message)
    {
        final var event = message.getEventType();

        for (final var evenListener : eventListeners) {
            if (event == evenListener.getEventType()) {
                evenListener.getConsumer().accept(message);
            }
        }
    }
}
