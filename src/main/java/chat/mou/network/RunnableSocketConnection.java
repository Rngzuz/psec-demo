package chat.mou.network;

import java.net.InetSocketAddress;

public interface RunnableSocketConnection extends Runnable, AutoCloseable
{
    boolean isOpen();
    void setAddress(InetSocketAddress address);
}
