package chat.mou.test;

import java.io.IOException;
import java.net.InetAddress;
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

public class Server implements AutoCloseable {
    class ClientSession {
        private AsynchronousSocketChannel channel;
        private final ReentrantLock lock;

        public ClientSession(AsynchronousSocketChannel channel) {
            this.channel = channel;
            this.lock = new ReentrantLock();
        }

        public AsynchronousSocketChannel getChannel() {
            return channel;
        }

        public ReentrantLock getLock() {
            return lock;
        }
    }

    private AsynchronousServerSocketChannel instance;
    private Map<String, ClientSession> clients = new ConcurrentHashMap<>();

    public Server(int port) throws IOException, InterruptedException {

        final var channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory()
        );

        instance = AsynchronousServerSocketChannel.open(channelGroup);
        instance.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));

        System.out.println("Server started on port " + port);

        final var acceptHandler = new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                instance.accept(null, this);

                try {
                    final var address = channel.getRemoteAddress().toString();
                    System.out.println("Connection established from: " + address);

                    clients.put(address, new ClientSession(channel));

                    final var inputBuffer = ByteBuffer.allocate(2048);

                    channel.read(inputBuffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer bytesRead, Void attachment) {
                            final var buffer = new byte[bytesRead];
                            inputBuffer.rewind();
                            inputBuffer.get(buffer);

                            final var result = new String(buffer);

                            clients.keySet().forEach(receiver -> {
                                final var session = clients.get(receiver);

                                if (session != null && session.getChannel() != null && session.getChannel().isOpen()) {
                                    session.getLock().lock();

                                    try {
                                        final var outputBuffer = ByteBuffer.wrap(result.getBytes());
                                        session.getChannel().write(outputBuffer).get();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    finally {
                                        session.getLock().unlock();
                                    }
                                }
                            });

                            inputBuffer.clear();
                            channel.read(inputBuffer, null, this);
                        }

                        @Override
                        public void failed(Throwable exc, Void attachment) {
                            clients.remove(address);
                            exc.printStackTrace();
                        }
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        };

        instance.accept(null, acceptHandler);
    }

    @Override
    public void close() throws Exception {
        if (instance != null && instance.isOpen()) {
            instance.close();
        }
    }
}
