package chat.mou.network;

import chat.mou.Message;
import chat.mou.events.AcceptEvent;
import chat.mou.events.ErrorEvent;
import chat.mou.events.ReadEvent;
import chat.mou.security.KeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Component
@Scope("singleton")
public final class HostSocketConnection extends SocketConnection
{
    private AsynchronousServerSocketChannel serverSocket;

    @Autowired
    public HostSocketConnection(ConfigurableApplicationContext applicationContext, KeyStore keyStore)
    {
        super(applicationContext, keyStore);
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

                    // Print encoded public key
                    System.out.println("Client public key:\n" + new String(message.getData()) + "\n");
                }
                else if (message.getType().equals(Message.Type.TEXT)) {
                    final var data = keyStore.decryptMessage(message.getData());
                    eventMulticaster.multicastEvent(new ReadEvent(this, data));

                    // Print encrypted and decrypted message data
                    System.out.println("Client encrypted message:\n" + new String(message.getData()) + "\n");
                    System.out.println("Client decrypted message:\n" + new String(data) + "\n");
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
            serverSocket = AsynchronousServerSocketChannel.open();
            serverSocket.bind(address);

            serverSocket.accept(null, acceptHandler);
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

        if (serverSocket != null && serverSocket.isOpen()) {
            serverSocket.close();
        }
    }

    @Override
    public boolean isOpen()
    {
        return serverSocket != null && serverSocket.isOpen() && clientChannel != null && clientChannel.isOpen();
    }

    @Override
    public boolean isHost()
    {
        return true;
    }

    @Override
    public String getSimplifiedAddress()
    {
        return address.toString();
    }
}
