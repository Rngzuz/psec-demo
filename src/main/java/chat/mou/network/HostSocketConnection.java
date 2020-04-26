package chat.mou.network;

import chat.mou.Message;
import chat.mou.events.AcceptEvent;
import chat.mou.events.ErrorEvent;
import chat.mou.events.MessageEvent;
import chat.mou.events.ReadEvent;
import chat.mou.security.KeyStore;
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
    private final KeyStore keyStore;

    private InetSocketAddress hostAddress;
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;

    @Autowired
    public HostSocketConnection(ConfigurableApplicationContext applicationContext, KeyStore keyStore)
    {
        eventMulticaster =
            applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
            ApplicationEventMulticaster.class
        );

        this.keyStore = keyStore;
    }

    private ApplicationListener<MessageEvent> onMessageEvent = this::_onMessageEvent;

    public void _onMessageEvent(MessageEvent event)
    {
        if (clientChannel != null && clientChannel.isOpen()) {
            try {
                final var message = new Message(Message.Type.TEXT, keyStore.encryptMessage(event.getBody()));
                clientChannel.write(ByteBuffer.wrap(Message.serialize(message)));
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private final CompletionHandler<AsynchronousSocketChannel, Void> acceptHandler = new CompletionHandler<>()
    {
        @Override
        public void completed(AsynchronousSocketChannel channel, Void attachment)
        {
            clientChannel = channel;

            eventMulticaster.multicastEvent(new AcceptEvent(this));
            eventMulticaster.addApplicationListener(onMessageEvent);

            final var inputBuffer = ByteBuffer.allocate(2048);
            clientChannel.read(inputBuffer, inputBuffer, readHandler);
        }

        @Override
        public void failed(Throwable exception, Void attachment)
        {
            eventMulticaster.multicastEvent(new ErrorEvent(this, ErrorEvent.Type.ACCEPT_ERROR));
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

            try {
                final var message = Message.deserialize(bytes);

                if (message.getType().equals(Message.Type.KEY)) {
                    keyStore.setAndDecodeExternalPublicKey(message.getData());

                    final var ownPublicKeyMessage = new Message(Message.Type.KEY, keyStore.getEncodedOwnPublicKey());
                    clientChannel.write(ByteBuffer.wrap(Message.serialize(ownPublicKeyMessage)));
                }
                else if (message.getType().equals(Message.Type.TEXT)) {
                    final var data = keyStore.decryptMessage(message.getData());
                    eventMulticaster.multicastEvent(new ReadEvent(this, data));
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            inputBuffer.clear();
            clientChannel.read(inputBuffer, inputBuffer, this);
        }

        @Override
        public void failed(Throwable exception, ByteBuffer attachment)
        {
            eventMulticaster.multicastEvent(new ErrorEvent(this, ErrorEvent.Type.READ_ERROR));
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
        eventMulticaster.removeApplicationListener(onMessageEvent);

        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
        }

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
