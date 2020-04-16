package chat.mou.messaging;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.function.Consumer;

public class ChatClient implements AutoCloseable, Runnable {
    private final InetSocketAddress hostAddress;
    private final Consumer<byte[]> consumer;
    private AsynchronousSocketChannel socketChannel;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ChatClient(InetSocketAddress hostAddress, Consumer<byte[]> consumer) {
        this.hostAddress = hostAddress;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try {
            socketChannel = AsynchronousSocketChannel.open();
            socketChannel.connect(hostAddress).get();


            in = new ObjectInputStream(Channels.newInputStream(socketChannel));
            out = new ObjectOutputStream(Channels.newOutputStream(socketChannel));


            final var inputBuffer = ByteBuffer.allocate(2048);

            while (socketChannel != null && socketChannel.isOpen()) {
                ChatMessage result;

                try {
                    result = (ChatMessage) in.readObject();
                }
                catch (ClassCastException exception) {
                    exception.printStackTrace();
                    continue;
                }


                // Blocking read to input buffer
                final var bytesRead = socketChannel.read(inputBuffer).get();

                // Break loop on code -1 ~ end-of-stream
                if (bytesRead == -1) {
                    break;
                }

                // Read incoming data into the input buffer
                final var temporaryBuffer = new byte[bytesRead];

                // Rewind buffer to read from the beginning
                inputBuffer.rewind();
                inputBuffer.get(temporaryBuffer);

                // Invoke read listener
                consumer.accept(temporaryBuffer);
                inputBuffer.clear();

                // Create string from byte array
                // final var result = new String(temporaryBuffer);
            }
        }
        catch (Throwable exception) {
            exception.printStackTrace();
            // Handle error
        }
    }

    public synchronized void sendMessage(String message) {
        final var outputBuffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(outputBuffer);
    }

    @Override
    public void close() throws IOException {
        if (socketChannel != null && socketChannel.isOpen()) {
            socketChannel.close();
        }
    }
}
