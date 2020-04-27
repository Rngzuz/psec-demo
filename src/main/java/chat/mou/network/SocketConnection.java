package chat.mou.network;

import chat.mou.Message;
import chat.mou.events.MessageEvent;
import chat.mou.security.KeyStore;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * An abstract representation of a connection established with an underlying socket channel.
 */
public abstract class SocketConnection implements Runnable, AutoCloseable
{
    protected final ApplicationEventMulticaster eventMulticaster;
    protected final KeyStore keyStore;

    protected InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);
    protected AsynchronousSocketChannel clientChannel;

    public SocketConnection(ConfigurableApplicationContext applicationContext, KeyStore keyStore)
    {
        eventMulticaster =
            applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
            ApplicationEventMulticaster.class
        );

        this.keyStore = keyStore;
    }

    protected ApplicationListener<MessageEvent> onMessageEvent = this::_onMessageEvent;

    /**
     * Handler method invoked when a {@link MessageEvent} has been published.
     *
     * @param event the event object received
     */
    protected void _onMessageEvent(MessageEvent event)
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

    public abstract String getSimplifiedAddress();

    public void setAddress(InetSocketAddress connectAddress)
    {
        this.address = connectAddress;
    }

    /**
     * Checks whether the underlying socket channels has been instantiated and opened.
     *
     * @return a flag indicating
     */
    public abstract boolean isOpen();

    public abstract boolean isHost();
}
