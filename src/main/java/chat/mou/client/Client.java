package chat.mou.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

public class Client implements Runnable, AutoCloseable
{
    private final InetSocketAddress connectionAddress;
    private AsynchronousSocketChannel clientChannel;

    public Client(InetSocketAddress connectionAddress)
    {
        this.connectionAddress = connectionAddress;
    }

    @Override
    public void run()
    {
        try {
            final var clientController = new ClientController();

            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(connectionAddress, null, new ConnectHandler(clientController, clientChannel));
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
}
