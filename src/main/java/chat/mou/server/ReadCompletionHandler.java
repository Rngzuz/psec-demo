package chat.mou.server;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class ReadCompletionHandler implements CompletionHandler<Integer, ReadCompletionAttachment> {
    @Override
    public void completed(Integer bytesRead, ReadCompletionAttachment attachment) {
        if (bytesRead == -1) return;

        final var channel = attachment.getChannel();
        final var addressToSessionMap = attachment.getAddressToSessionMap();
        final var inputBuffer = attachment.getInputBuffer();

        // Byte array for storing result from the buffer
        final var result = new byte[bytesRead];

        // Rewind buffer to read from the beginning
        inputBuffer.rewind();
        inputBuffer.get(result);

        // Print result as String
        System.out.println(new String(result));

        // Loop through all connected clients and send the result to them
        for (final var key : addressToSessionMap.keySet()) {
            final var session = addressToSessionMap.get(key);

            if (session != null && session.getChannel() != null && session.getChannel().isOpen()) {
                // Lock object
                session.getLock().lock();

                try {
                    // Write output result and wait for future completion
                    final var outputBuffer = ByteBuffer.wrap(result);
                    session.getChannel().write(outputBuffer).get();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                    addressToSessionMap.remove(key);
                }
                finally {
                    session.getLock().unlock();
                }
            }
        }

        // Clear inputBuffer and ready it for more messages
        inputBuffer.clear();

        // Attach same completion handler instance and await next message
        channel.read(inputBuffer, null, this);
    }

    @Override
    public void failed(Throwable exception, ReadCompletionAttachment attachment) {
        // Failed reading from interface socket channel
        exception.printStackTrace();
    }
}
