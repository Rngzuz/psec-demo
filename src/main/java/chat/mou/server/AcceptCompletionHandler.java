package chat.mou.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AcceptCompletionAttachment> {
    @Override
    public void completed(AsynchronousSocketChannel channel, AcceptCompletionAttachment attachment) {
        final var serverChannel = attachment.getServerChannel();
        final var addressToSessionMap = attachment.getAddressToSessionMap();

        serverChannel.accept(attachment, this);

        try {
            // Save connection by IP address for further use
            final var address = channel.getRemoteAddress().toString();
            addressToSessionMap.put(address, new ClientSession(address, channel));

            System.out.println(address + " connected");

            final var inputBuffer = ByteBuffer.allocate(2048);
            channel.read(inputBuffer, new ReadCompletionAttachment(channel, addressToSessionMap, inputBuffer), new ReadCompletionHandler());
        } catch (IOException exception) {
            // Failed to get remote address
            exception.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exception, AcceptCompletionAttachment attachment) {
        // Failed on accepting incoming connection
        exception.printStackTrace();
    }
}
