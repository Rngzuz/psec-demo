package chat.mou.client;

import chat.mou.shared.EventBus;
import chat.mou.shared.EventType;
import chat.mou.shared.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public final class Client implements Runnable, AutoCloseable
{
    private final InetSocketAddress connectionAddress;
    private final EventBus eventBus;

    private AsynchronousSocketChannel clientChannel;

    public Client(InetSocketAddress connectionAddress)
    {
        this.connectionAddress = connectionAddress;
        this.eventBus = new EventBus();

        eventBus.addListener(EventType.WRITE, message -> {
            try {
                final var rawMessage = new Message(EventType.BROADCAST, message.getBody()).serialize();
                clientChannel.write(ByteBuffer.wrap(rawMessage));
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void run()
    {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(connectionAddress, null, new ConnectHandler(eventBus, clientChannel));
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
        }
    }

    public boolean isOpen() {
        return clientChannel != null && clientChannel.isOpen();
    }

    public EventBus getEventBus()
    {
        return eventBus;
    }
}
