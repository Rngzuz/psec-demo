package chat.mou.network;

import chat.mou.Message;
import chat.mou.events.ConnectEvent;
import chat.mou.events.ErrorEvent;
import chat.mou.events.ReadEvent;
import chat.mou.security.KeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Component
@Scope("singleton")
public class ClientSocketConnection extends SocketConnection
{
    @Autowired
    public ClientSocketConnection(ConfigurableApplicationContext applicationContext, KeyStore keyStore)
    {
        super(applicationContext, keyStore);
    }

    private final CompletionHandler<Void, Void> connectHandler = new CompletionHandler<>()
    {
        @Override
        public void completed(Void result, Void attachment)
        {
            try {
                final var ownPublicKeyMessage = new Message(Message.Type.KEY, keyStore.getEncodedOwnPublicKey());
                clientChannel.write(ByteBuffer.wrap(Message.serialize(ownPublicKeyMessage)));

                eventMulticaster.multicastEvent(new ConnectEvent(this));
                eventMulticaster.addApplicationListener(onMessageEvent);

                final var inputBuffer = ByteBuffer.allocate(2048);
                clientChannel.read(inputBuffer, inputBuffer, readHandler);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exception, Void attachment)
        {
            eventMulticaster.multicastEvent(new ErrorEvent(this, ErrorEvent.Type.CONNECT_ERROR));
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

                    // Print encoded public key
                    System.out.println("Host public key:\n" + new String(message.getData()) + "\n");
                }
                else if (message.getType().equals(Message.Type.TEXT)) {
                    final var data = keyStore.decryptMessage(message.getData());
                    eventMulticaster.multicastEvent(new ReadEvent(this, data));

                    // Print encrypted and decrypted message data
                    System.out.println("Host encrypted message:\n" + new String(message.getData()) + "\n");
                    System.out.println("Host decrypted message:\n" + new String(data) + "\n");
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
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(address, null, connectHandler);
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
    }

    @Override
    public boolean isOpen()
    {
        return clientChannel != null && clientChannel.isOpen();
    }

    @Override
    public boolean isHost()
    {
        return false;
    }

    @Override
    public String getSimplifiedAddress()
    {
        try {
            return clientChannel.getRemoteAddress().toString();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        return address.toString();
    }
}
