package chat.mou.server;

import chat.mou.shared.EventBus;
import chat.mou.shared.EventType;
import chat.mou.shared.Message;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public final class Server implements Runnable, AutoCloseable
{
    private final InetSocketAddress bindAddress;
    private final EventBus eventBus;
    private final Map<String, Session> sessionMap;

    private AsynchronousServerSocketChannel serverChannel;

    public Server(InetSocketAddress bindAddress)
    {
        this.bindAddress = bindAddress;
        this.eventBus = new EventBus();
        sessionMap = new ConcurrentHashMap<>();

        eventBus.addListener(EventType.BROADCAST, message -> {
            try {
                final var messageBytes = message.serialize();

                // Loop through sessions and send the message
                for (final var address : sessionMap.keySet()) {
                    final var session = sessionMap.get(address);
                    System.out.println(address + ": " + message.getBody());

                    if (session.getChannel() != null && session.getChannel().isOpen()) {
                        session.getLock().lock();

                        try {
                            // Write output result and wait for future completion
                            session.getChannel().write(ByteBuffer.wrap(messageBytes)).get();
                        }
                        finally {
                            session.getLock().unlock();
                        }
                    }
                }
            }
            catch (IOException | InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void run()
    {
        try {
            // Create a channel group with a fixed thread pool available runtime
            final var group = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory()
            );

            // Open server channel registered to use the channel group
            serverChannel = AsynchronousServerSocketChannel.open(group);

            // Bind server to an IP address
            serverChannel.bind(bindAddress);
            System.out.println("Server started and listening on " + bindAddress.toString());

            // Register connection accept completion handler
            serverChannel.accept(null, new AcceptHandler(eventBus, sessionMap, serverChannel));
        }
        catch (IOException exception) {
            // Failed to start host
            exception.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        // Attempt to close server channel
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }

    public EventBus getEventBus()
    {
        return eventBus;
    }
}
