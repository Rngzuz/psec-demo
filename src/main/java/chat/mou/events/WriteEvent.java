package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class WriteEvent extends ApplicationEvent
{
    public WriteEvent(Object source)
    {
        super(source);
    }
}
