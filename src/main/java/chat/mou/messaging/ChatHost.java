package chat.mou.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ChatHost implements AutoCloseable {
    // Class used for saving open socket channel connections
    private static class SocketChannelSession {
        private AsynchronousSocketChannel socketChannel;
        private final ReentrantLock lock;

        public SocketChannelSession(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            this.lock = new ReentrantLock();
        }

        public AsynchronousSocketChannel getChannel() {
            return socketChannel;
        }

        public ReentrantLock getLock() {
            return lock;
        }
    }

    private final InetSocketAddress hostAddress;
    private final Map<String, SocketChannelSession> addressToSocketChannelMap = new ConcurrentHashMap<>();

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
        public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
            serverSocketChannel.accept(null, this);

            try {
                // Save connection by IP address for further use
                final var address = socketChannel.getRemoteAddress().toString();
                addressToSocketChannelMap.put(address, new SocketChannelSession(socketChannel));

                System.out.println(address + " connected");

                final var inputBuffer = ByteBuffer.allocate(2048);
                socketChannel.read(inputBuffer, new ReadCompletionProperties(address, inputBuffer, socketChannel), readCompletionHandler);
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

    private static class ReadCompletionProperties {
        public final String address;
        public final ByteBuffer inputBuffer;
        public final AsynchronousSocketChannel socketChannel;

        public ReadCompletionProperties(String address, ByteBuffer inputBuffer, AsynchronousSocketChannel socketChannel) {
            this.address = address;
            this.inputBuffer = inputBuffer;
            this.socketChannel = socketChannel;
        }
    }

    private final CompletionHandler<Integer, ReadCompletionProperties> readCompletionHandler = new CompletionHandler<>() {
        @Override
        public void completed(Integer bytesRead, ReadCompletionProperties properties) {
            // Disconnect
            if (bytesRead == -1) {
                addressToSocketChannelMap.remove(properties.address);
                return;
            }

            // Read incoming data into the input buffer
            final var temporaryBuffer = new byte[bytesRead];

            // Rewind buffer to read from the beginning
            properties.inputBuffer.rewind();
            properties.inputBuffer.get(temporaryBuffer);

            // Create string from byte array
            final var result = new String(temporaryBuffer);

            System.out.println(properties.address + ": " + result);

            for (final var key : addressToSocketChannelMap.keySet()) {
                // Get current client session
                final var socketChannelSession = addressToSocketChannelMap.get(key);

                if (socketChannelSession != null && socketChannelSession.getChannel() != null && socketChannelSession.getChannel().isOpen()) {
                    // Lock object
                    socketChannelSession.getLock().lock();

                    try {
                        // Write output result and wait for future completion
                        final var outputBuffer = ByteBuffer.wrap(result.getBytes());
                        socketChannelSession.getChannel().write(outputBuffer).get();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    } finally {
                        socketChannelSession.getLock().unlock();
                    }
                }
            }

            properties.inputBuffer.clear();

            // Attach read listener to this connection to listen for incoming messages
            properties.socketChannel.read(properties.inputBuffer, properties, this);
        }

        @Override
        public void failed(Throwable exception, ReadCompletionProperties properties) {
            // Failed reading from client socket channel
            exception.printStackTrace();
        }
    };

    @Override
    public void close() throws IOException {
        if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
    }
}
