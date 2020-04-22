package chat.mou.server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class used for saving open socket channel connections.
 */
public final class Session
{
    private final String address;
    private String displayName;
    private final AsynchronousSocketChannel socketChannel;
    private final ReentrantLock lock;

    public Session(String address, AsynchronousSocketChannel socketChannel)
    {
        this.address = address;
        this.socketChannel = socketChannel;
        this.lock = new ReentrantLock();
    }

    public String getAddress()
    {
        return address;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public AsynchronousSocketChannel getChannel()
    {
        return socketChannel;
    }

    public ReentrantLock getLock()
    {
        return lock;
    }
}
