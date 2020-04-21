package chat.mou.client;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadHandler implements CompletionHandler<Integer, Void>
{
    private final ClientController clientController;
    private final AsynchronousSocketChannel clientChannel;

    public ReadHandler(ClientController clientController, AsynchronousSocketChannel clientChannel)
    {
        this.clientController = clientController;
        this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Integer bytesRead, Void attachment)
    {

    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {

    }
}
