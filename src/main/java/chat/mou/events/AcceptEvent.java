package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class AcceptEvent extends ApplicationEvent
{
    public AcceptEvent(Object source)
    {
        super(source);
    }
}
