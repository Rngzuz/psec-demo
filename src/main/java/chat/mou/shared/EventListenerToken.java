package chat.mou.shared;

public interface EventListenerToken
{
    int getIndex();
    EventListener getListener();
    boolean removeListener();
}
