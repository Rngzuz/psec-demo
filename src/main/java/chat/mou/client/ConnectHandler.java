package chat.mou.client;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ConnectHandler implements CompletionHandler<Void, Void>
{
    private final ClientController clientController;
    private final AsynchronousSocketChannel clientChannel;

    public ConnectHandler(ClientController clientController, AsynchronousSocketChannel clientChannel)
    {
        this.clientController = clientController;
        this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Void result, Void attachment)
    {

    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {

    }
}
