package chat.mou.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ChatHost implements AutoCloseable {
    // Class used for saving open socket channel connections
    private static class SocketChannelSession {
        private AsynchronousSocketChannel channel;
        private final ReentrantLock lock;

        public SocketChannelSession(AsynchronousSocketChannel socketChannel) {
            this.channel = socketChannel;
            this.lock = new ReentrantLock();
        }

        public AsynchronousSocketChannel getChannel() {
            return channel;
        }

        public ReentrantLock getLock() {
            return lock;
        }
    }

    private final InetSocketAddress hostAddress;
    private final Map<String, SocketChannelSession> addressToSessionMap = new ConcurrentHashMap<>();

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    public ChatHost(InetSocketAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void open() throws IOException {
        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
        serverSocketChannel.bind(hostAddress);

        System.out.println("Started chat host on " + hostAddress.getHostName() + ":" + hostAddress.getPort());

        serverSocketChannel.accept(null, acceptCompletionHandler);
    }

    private final CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHandler = new CompletionHandler<>() {
        @Override
        public void completed(AsynchronousSocketChannel channel, Void attachment) {
            serverSocketChannel.accept(null, this);

            try {
                // Save connection by IP address for further use
                final var address = channel.getRemoteAddress().toString();
                addressToSessionMap.put(address, new SocketChannelSession(channel));

                System.out.println(address + " connected");

                final var inputBuffer = ByteBuffer.allocate(2048);
                channel.read(inputBuffer, null, createReadHandler(inputBuffer, channel));
            } catch (IOException exception) {
                // Failed to get remote address
                exception.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exception, Void attachment) {
            // Failed on accepting incoming connection
            exception.printStackTrace();
        }
    };

    private CompletionHandler<Integer, Void> createReadHandler(ByteBuffer inputBuffer, AsynchronousSocketChannel channel) {
        return new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) return;

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
            public void failed(Throwable exception, Void attachment) {
                // Failed reading from interface socket channel
                exception.printStackTrace();
            }
        };
    }

    @Override
    public void close() throws IOException {
        if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
    }
}
