package chat.mou.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ChatServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverChannel;

    private final ByteBuffer buffer = ByteBuffer.allocate(256);
    private final ByteBuffer greeting = ByteBuffer.wrap("You've connected!".getBytes());

    // Multiplexor explanation: https://www.javatpoint.com/java-nio-selector
    public ChatServer(Integer port) throws IOException {
        serverChannel = ServerSocketChannel.open();

        // Bind the loopback IP address to the socket
        serverChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));

        // Configure blocking operations to be non-blocking (e.g. the .accept() method)
        serverChannel.configureBlocking(false);

        // Create a selector for channels connected to the server
        selector = Selector.open();

        // Register the selector to the ServerSocketChannel
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    // Run the server
    @Override
    public void run() {
        try {
            System.out.println("Starting server on " + serverChannel.getLocalAddress());

            Iterator<SelectionKey> iterator;
            SelectionKey key;

            while (serverChannel.isOpen()) {
                // Blocks and will wake up on incoming connection
                selector.select();

                // Get selected keys
                iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        handleAcceptable(key);
                    }

                    if (key.isReadable()) {
                        handleReadable(key);
                    }
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void handleAcceptable(SelectionKey key) throws IOException {
        final var client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);

        final var address = String.format(
                "%s:%s",
                client.socket().getInetAddress().toString(),
                client.socket().getPort()
        );

        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, address);
        client.write(greeting);
        greeting.rewind();

        System.out.println("Accepted connection from " + address);
    }

    private void handleReadable(SelectionKey key) throws IOException {
        final var client = (SocketChannel) key.channel();
        final var builder = new StringBuilder();

        buffer.clear();

        var read = 0;

        try {
            while ((read = client.read(buffer)) > 0) {
                buffer.flip();
                builder.append(new String(buffer.array()));
                buffer.clear();
            }
        }
        catch (Exception e) {
            // On error or in case of client disconnect
            key.cancel();
            read = -1;
        }

        String message;

        if (read < 0) {
            message = key.attachment() + " left the channel.\n";
            client.close();
        }
        else {
            message = key.attachment() + ": " + builder.toString();
        }

        broadcastMessage(message);
    }

    private  void broadcastMessage(String message) throws IOException {
        final var messageBuffer = ByteBuffer.wrap(message.getBytes());

        for (final var key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                final var client = (SocketChannel) key.channel();

                System.out.println(message);

//                System.out.println(String.format(
//                    "Message: %s\nAddress: %s\nReadable: %s\nWritable: %s\n",
//                    message,
//                    client.getLocalAddress(),
//                    key.isReadable(),
//                    key.isWritable()
//                ));

                client.write(messageBuffer);
                messageBuffer.rewind();
            }
        }

        System.out.println("Finished broadcast.");
    }
}
