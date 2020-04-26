package chat.mou.network;

import chat.mou.events.AcceptEvent;
import chat.mou.events.MessageEvent;
import chat.mou.events.ReadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Component
@Scope("singleton")
public class HostSocketConnection implements RunnableSocketConnection
{
    private final ApplicationEventMulticaster eventMulticaster;

    private InetSocketAddress hostAddress;
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;

    @Autowired
    public HostSocketConnection(ConfigurableApplicationContext applicationContext)
    {
        eventMulticaster =
            applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
            ApplicationEventMulticaster.class
        );
    }

    private final ApplicationListener<MessageEvent> messageEventListener = messageEvent -> {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.write(ByteBuffer.wrap(messageEvent.getBody().getBytes()));
        }
    };

    private final CompletionHandler<AsynchronousSocketChannel, Void> acceptHandler = new CompletionHandler<>()
    {
        @Override
        public void completed(AsynchronousSocketChannel channel, Void attachment)
        {
            clientChannel = channel;

            eventMulticaster.multicastEvent(new AcceptEvent(this));
            eventMulticaster.addApplicationListener(messageEventListener);

            final var inputBuffer = ByteBuffer.allocate(2048);
            clientChannel.read(inputBuffer, inputBuffer, readHandler);
        }

        @Override
        public void failed(Throwable exception, Void attachment)
        {
            exception.printStackTrace();
        }
    };

    private final CompletionHandler<Integer, ByteBuffer> readHandler = new CompletionHandler<>()
    {
        @Override
        public void completed(Integer bytesRead, ByteBuffer inputBuffer)
        {
            if (bytesRead == -1) {
                return;
            }

            final var bytes = new byte[bytesRead];
            inputBuffer.rewind();
            inputBuffer.get(bytes);

            eventMulticaster.multicastEvent(new ReadEvent(this, bytes));

            inputBuffer.clear();
            clientChannel.read(inputBuffer, null, this);
        }

        @Override
        public void failed(Throwable exception, ByteBuffer attachment)
        {
            exception.printStackTrace();
        }
    };

    @Override
    public void run()
    {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(hostAddress);

            serverChannel.accept(null, acceptHandler);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        eventMulticaster.removeApplicationListener(messageEventListener);

        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }

    @Override
    public boolean isOpen()
    {
        return serverChannel != null && serverChannel.isOpen() && clientChannel != null && clientChannel.isOpen();
    }

    @Override
    public void setAddress(InetSocketAddress hostAddress)
    {
        this.hostAddress = hostAddress;
    }
}
