package chat.mou.messaging;

import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Function;

public class ChatClient implements AutoCloseable, Runnable {
    private final InetSocketAddress hostAddress;
    private AsynchronousSocketChannel socketChannel;

    private PropertyChangeSupport support;

    public ChatClient(InetSocketAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void subscribe(Function<String, Void> callback) {

    }

    @Override
    public void run() {
        try {
            socketChannel = AsynchronousSocketChannel.open();
            socketChannel.connect(hostAddress);

            final var inputBuffer = ByteBuffer.allocate(2048);

            while (socketChannel != null && socketChannel.isOpen()) {
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

                // Create string from byte array
                final var result = new String(temporaryBuffer);
            }
        }
        catch (Throwable exception) {
            // Handle error
        }
    }

    private static class WriteCompletionProperties {
        public final ByteBuffer outputBuffer;
        public final AsynchronousSocketChannel socketChannel;

        public WriteCompletionProperties(ByteBuffer outputBuffer, AsynchronousSocketChannel socketChannel) {
            this.outputBuffer = outputBuffer;
            this.socketChannel = socketChannel;
        }
    }

    private final CompletionHandler<Integer, WriteCompletionProperties> writeCompletionHandler = new CompletionHandler<>() {
        @Override
        public void completed(Integer bytesWritten, WriteCompletionProperties properties) {

        }

        @Override
        public void failed(Throwable exception, WriteCompletionProperties properties) {
            exception.printStackTrace();
        }
    };

    @Override
    public void close() throws IOException {
        if (socketChannel != null && socketChannel.isOpen()) {
            socketChannel.close();
        }
    }
}
