package chat.mou.network;

import chat.mou.events.ConnectEvent;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Component
@Scope("singleton")
public class ClientSocketConnection implements RunnableSocketConnection
{
    private final ApplicationEventMulticaster eventMulticaster;

    private InetSocketAddress connectAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);
    private AsynchronousSocketChannel clientChannel;

    @Autowired
    public ClientSocketConnection(ConfigurableApplicationContext applicationContext)
    {
        eventMulticaster =
            applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
            ApplicationEventMulticaster.class
        );

        applicationContext.addApplicationListener(event -> {
            if (clientChannel != null && clientChannel.isOpen() && event instanceof MessageEvent) {
                final var body = ((MessageEvent) event).getBody();
                clientChannel.write(ByteBuffer.wrap(body.getBytes()));
            }
        });
    }

    private final ApplicationListener<MessageEvent> messageEventListener = messageEvent -> {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.write(ByteBuffer.wrap(messageEvent.getBody().getBytes()));
        }
    };

    private final CompletionHandler<Void, Void> connectHandler = new CompletionHandler<>()
    {
        @Override
        public void completed(Void result, Void attachment)
        {
            eventMulticaster.multicastEvent(new ConnectEvent(this));
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
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(connectAddress, null, connectHandler);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        eventMulticaster.removeApplicationListener(messageEventListener);

        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
        }
    }

    @Override
    public boolean isOpen() {
        return clientChannel != null && clientChannel.isOpen();
    }

    @Override
    public void setAddress(InetSocketAddress connectAddress)
    {
        this.connectAddress = connectAddress;
    }
}
