package chat.mou.client;

import chat.mou.shared.EventType;
import chat.mou.shared.EventBus;
import chat.mou.shared.Message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public final class ReadHandler implements CompletionHandler<Integer, Void>
{
    private final EventBus eventBus;
    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer inputBuffer;

    public ReadHandler(
        EventBus eventBus, AsynchronousSocketChannel clientChannel, ByteBuffer inputBuffer
    )
    {
        this.eventBus = eventBus;
        this.clientChannel = clientChannel;
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void completed(Integer bytesRead, Void attachment)
    {
        // Disconnect client on end-of-stream
        if (bytesRead == -1) {
            return;
        }

        // Byte array for storing result from the buffer
        final var rawMessage = new byte[bytesRead];

        // Rewind buffer to read from the beginning
        inputBuffer.rewind();
        inputBuffer.get(rawMessage);

        // Deserialize and dispatch event
        eventBus.dispatch(Message.deserialize(rawMessage));

        // Attach same completion handler instance and await next message
        inputBuffer.clear();
        clientChannel.read(inputBuffer, null, this);
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        exception.printStackTrace();
    }
}
