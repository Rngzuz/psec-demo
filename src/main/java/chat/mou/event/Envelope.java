package chat.mou.event;

import java.io.Serializable;

public final class Envelope
{
    private final Event event;
    private final Class<? extends Serializable> object;

    public Envelope(Event event, Class<? extends Serializable> object)
    {
        this.event = event;
        this.object = object;
    }

    public Event getEvent()
    {
        return event;
    }

    public Class<? extends Serializable> getObject()
    {
        return object;
    }
}
