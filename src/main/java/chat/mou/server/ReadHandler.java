package chat.mou.server;

import chat.mou.shared.Message;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadHandler implements CompletionHandler<Integer, Void>
{
    private final ServerController serverController;
    private final AsynchronousSocketChannel clientChannel;
    private final Session clientSession;
    private final ByteBuffer inputBuffer;

    public ReadHandler(
        ServerController serverController,
        AsynchronousSocketChannel clientChannel,
        Session clientSession,
        ByteBuffer inputBuffer
    )
    {
        this.serverController = serverController;
        this.clientChannel = clientChannel;
        this.clientSession = clientSession;
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void completed(Integer bytesRead, Void attachment)
    {
        // Disconnect client on end-of-stream
        if (bytesRead == -1) {
            serverController.removeSession(clientSession);
            return;
        }

        // Byte array for storing result from the buffer
        final var rawMessage = new byte[bytesRead];

        // Rewind buffer to read from the beginning
        inputBuffer.rewind();
        inputBuffer.get(rawMessage);

        // Deserialize message to a message object
        final var message = Message.deserialize(rawMessage);

        // Print message body
        System.out.println(message.getBody());

        // Switch on message action
        switch (message.getAction()) {
            case CONNECT:
                clientSession.setDisplayName(message.getBody());
                break;
            case DISCONNECT:
                serverController.removeSession(clientSession);
                return;
            case BROADCAST:
                serverController.broadcastMessage(message);
                break;
        }

        // Attach same completion handler instance and await next message
        inputBuffer.clear();
        clientChannel.read(inputBuffer, null, this);
    }

    @Override
    public void failed(Throwable exception, Void attachment)
    {
        // Failed reading from ui socket channel
        exception.printStackTrace();
    }
}
