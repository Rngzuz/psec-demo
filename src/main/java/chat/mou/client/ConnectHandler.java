package chat.mou.client;

import chat.mou.shared.EventBus;
import chat.mou.shared.EventType;
import chat.mou.shared.Message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public final class ConnectHandler implements CompletionHandler<Void, Void>
{
    private final EventBus eventBus;
    private final AsynchronousSocketChannel clientChannel;

    public ConnectHandler(EventBus eventBus, AsynchronousSocketChannel clientChannel)
    {
        this.eventBus = eventBus;
        this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Void result, Void attachment)
    {
        final var inputBuffer = ByteBuffer.allocate(2048);
        clientChannel.read(inputBuffer, null, new ReadHandler(eventBus, clientChannel, inputBuffer));
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        exception.printStackTrace();
    }
}
