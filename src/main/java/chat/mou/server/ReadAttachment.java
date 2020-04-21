package chat.mou.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class ReadAttachment
{
    private final ServerController serverController;

    private final String address;
    private final AsynchronousSocketChannel channel;
    private final Map<String, Session> sessionMap;
    private final ByteBuffer inputBuffer;

    public ReadAttachment(
        ServerController serverController,
        String address,
        AsynchronousSocketChannel channel,
        Map<String, Session> sessionMap,
        ByteBuffer inputBuffer
    )
    {
        this.serverController = serverController;
        this.address = address;
        this.channel = channel;
        this.sessionMap = sessionMap;
        this.inputBuffer = inputBuffer;
    }

    public String getAddress()
    {
        return address;
    }

    public ServerController getServerController()
    {
        return serverController;
    }

    public AsynchronousSocketChannel getChannel()
    {
        return channel;
    }

    public Map<String, Session> getSessionMap()
    {
        return sessionMap;
    }

    public ByteBuffer getInputBuffer()
    {
        return inputBuffer;
    }
}
