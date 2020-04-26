package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class MessageEvent extends ApplicationEvent
{
    private final String body;

    public MessageEvent(Object source, String body)
    {
        super(source);
        this.body = body;
    }

    public String getBody()
    {
        return body;
    }
}
