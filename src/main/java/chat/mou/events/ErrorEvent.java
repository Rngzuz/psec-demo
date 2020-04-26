package chat.mou.events;

import org.springframework.context.ApplicationEvent;

public class ErrorEvent extends ApplicationEvent
{
    public enum Type {
        ACCEPT_ERROR("Could not establish connection with client"),
        CONNECT_ERROR("Could not connect to host"),
        READ_ERROR("read error"),
        WRITE_ERROR("write error"),
        DISCONNECT_ERROR("disconnect error");

        private final String message;

        Type(String message) {
            this.message = message;
        }

        public String getMessage()
        {
            return message;
        }
    }

    private final Type type;

    public ErrorEvent(Object source, Type type)
    {
        super(source);
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }

    public String getMessage()
    {
        return getType().getMessage();
    }
}
