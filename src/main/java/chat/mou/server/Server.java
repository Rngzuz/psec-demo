package chat.mou.server;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class Server implements Runnable, AutoCloseable
{
    private AsynchronousServerSocketChannel serverChannel;

    @Override
    public void run()
    {
        try {
            // Create concurrent hash map to facilitate sessions
            final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

            final var actionController = new ServerController();

            // Create a channel group with a fixed thread pool available runtime
            final var group = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory()
            );

            // Open server channel registered to use the channel group
            serverChannel = AsynchronousServerSocketChannel.open(group);

            // Bind server on loopback address (usually localhost) using port 8080
            serverChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080));

            System.out.println("Server started and listening on " + InetAddress.getLoopbackAddress() + ":8080");

            // Register connection accept completion handler
            serverChannel.accept(null, new AcceptHandler(actionController, serverChannel));
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
}
