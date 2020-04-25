package chat.mou.event.message;

import java.io.Serializable;

public final class TextMessage implements Serializable
{
    private final String sender;
    private final String text;

    public TextMessage(String sender, String text)
    {
        this.sender = sender;
        this.text = text;
    }

    public String getSender()
    {
        return sender;
    }

    public String getText()
    {
        return text;
    }
}
