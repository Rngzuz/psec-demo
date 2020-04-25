package chat.mou.server;

import chat.mou.shared.EventBus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

public final class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void>
{
    private final EventBus eventBus;
    private final Map<String, Session> sessionMap;
    private final AsynchronousServerSocketChannel serverChannel;

    public AcceptHandler(
        EventBus eventBus, Map<String, Session> sessionMap, AsynchronousServerSocketChannel serverChannel
    )
    {
        this.eventBus = eventBus;
        this.sessionMap = sessionMap;
        this.serverChannel = serverChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel clientChannel, Void attachment)
    {
        // Re-use accept handler and listen for next potential connection
        serverChannel.accept(attachment, this);

        try {
            // Get client IP address and create client session
            final var clientAddress = clientChannel.getRemoteAddress().toString();
            final var clientSession = new Session(clientAddress, clientChannel);
            sessionMap.put(clientAddress, clientSession);

            final var inputBuffer = ByteBuffer.allocate(2048);
            clientChannel.read(inputBuffer,
                null,
                new ReadHandler(eventBus, sessionMap, clientChannel, clientSession, inputBuffer)
            );
        }
        catch (IOException exception) {
            // Failed to get remote address
            exception.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        // Failed on accepting incoming connection
        exception.printStackTrace();
    }
}
