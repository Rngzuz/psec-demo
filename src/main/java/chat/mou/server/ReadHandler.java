package chat.mou.server;

import chat.mou.shared.EventBus;
import chat.mou.shared.EventType;
import chat.mou.shared.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

public final class ReadHandler implements CompletionHandler<Integer, Void>
{
    private final EventBus eventBus;
    private final Map<String, Session> sessionMap;
    private final AsynchronousSocketChannel clientChannel;
    private final Session clientSession;
    private final ByteBuffer inputBuffer;

    public ReadHandler(
        EventBus eventBus,
        Map<String, Session> sessionMap,
        AsynchronousSocketChannel clientChannel,
        Session clientSession,
        ByteBuffer inputBuffer
    )
    {
        this.eventBus = eventBus;
        this.sessionMap = sessionMap;
        this.clientChannel = clientChannel;
        this.clientSession = clientSession;
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void completed(Integer bytesRead, Void attachment)
    {
        // Disconnect client on end-of-stream
        if (bytesRead == -1) {
            sessionMap.remove(clientSession.getAddress());
            return;
        }

        // Byte array for storing result from the buffer
        final var rawMessage = new byte[bytesRead];

        // Rewind buffer to read from the beginning
        inputBuffer.rewind();
        inputBuffer.get(rawMessage);

        final var message = Message.deserialize(rawMessage);

        // Deserialize message to a message and dispatch
        eventBus.dispatch(message);
        inputBuffer.clear();

        // Attach same completion handler instance and await next message
        clientChannel.read(inputBuffer, null, this);
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        // Failed reading from ui socket channel
        exception.printStackTrace();
    }
}
