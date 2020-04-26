package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class ReadEvent extends ApplicationEvent
{
    private final byte[] body;

    public ReadEvent(Object source, byte[] body)
    {
        super(source);
        this.body = body;
    }

    public byte[] getBody()
    {
        return body;
    }
}
