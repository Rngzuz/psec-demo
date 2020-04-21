package chat.mou.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void>
{
    private final ServerController serverController;
    private final AsynchronousServerSocketChannel serverChannel;

    public AcceptHandler(ServerController serverController, AsynchronousServerSocketChannel serverChannel)
    {
        this.serverController = serverController;
        this.serverChannel = serverChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel clientChannel, Void attachment)
    {
        // Re-use accept handler and listen for next potential connection
        serverChannel.accept(attachment, this);

        try {
            // Get client IP address and create client session
            final var clientAddress = clientChannel.getLocalAddress().toString();
            final var clientSession = new Session(clientAddress, clientChannel);
            serverController.putSession(clientSession);

            System.out.println(clientAddress + " connected");

            final var inputBuffer = ByteBuffer.allocate(2048);
            clientChannel.read(
                inputBuffer,
                null,
                new ReadHandler(serverController, clientChannel, clientSession, inputBuffer)
            );
        }
        catch (IOException exception) {
            // Failed to get remote address
            exception.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        // Failed on accepting incoming connection
        exception.printStackTrace();
    }
}
