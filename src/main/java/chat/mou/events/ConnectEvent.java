package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class ConnectEvent extends ApplicationEvent
{
    public ConnectEvent(Object source)
    {
        super(source);
    }
}
