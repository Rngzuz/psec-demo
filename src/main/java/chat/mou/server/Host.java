package chat.mou.server;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Host implements Runnable, AutoCloseable {
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private Map<String, ClientSession> addressToSessionMap;

    public void addClient(ClientSession session) {
        if (addressToSessionMap == null) return;
        addressToSessionMap.put(session.getAddress(), session);
    }

    public void removeClient(String address) {
        if (addressToSessionMap == null) return;
        addressToSessionMap.remove(address);
    }

    public void broadcastMessage(byte[] message) {
        if (serverChannel == null) return;

        final var buffer = ByteBuffer.wrap(message);

        for (final var key : addressToSessionMap.keySet()) {
            final var session = addressToSessionMap.get(key);
            final var channel = session.getChannel();

            if (channel != null && channel.isOpen()) {
                // Lock object
                session.getLock().lock();

                try {
                    // Write message and wait for future to complete
                    channel.write(buffer).get();
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
    }

    public byte[] serializeMessage(Object message) throws IOException {
        // Write the object to an in-memory stream and get resulting bytes
        final var stream = new ByteArrayOutputStream();
        final var objectStream = new ObjectOutputStream(stream);

        objectStream.writeObject(message);

        return stream.toByteArray();
    }

    public Object deserializeMessage(byte[] message) throws IOException, ClassNotFoundException {
        final var stream = new ByteArrayInputStream(message);
        final var objectStream = new ObjectInputStream(stream);

        return objectStream.readObject();
    }

    @Override
    public void run() {
        try {
            addressToSessionMap = new ConcurrentHashMap<>();

            // Create a channel group with a fixed thread pool available runtime
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());

            // Open server channel registered to use the channel group
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);

            // Bind server on loopback address (usually localhost) using port 8080
            serverChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080));

            // Register connection accept completion handler
            serverChannel.accept(new AcceptCompletionAttachment(serverChannel, addressToSessionMap), new AcceptCompletionHandler());
        } catch (IOException exception) {
            // Failed to start host
            exception.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // Attempt to close server channel
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }
}
