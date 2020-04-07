package chat.mou.messaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChatClientStart {
    public static void main(String[] args) {
        final var hostAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);

        final Consumer<byte[]> resultConsumer = (byte[] bytes) -> System.out.println(new String(bytes));

        try (final var chatClient = new ChatClient(hostAddress, resultConsumer)) {
            // Run chat client on separate thread
            final var executor = Executors.newSingleThreadExecutor();
            executor.submit(chatClient);

            final var reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                final var userMessage = reader.readLine();
                chatClient.sendMessage(userMessage);

                if (userMessage.equals("-S")) {
                    chatClient.close();
                    break;
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
