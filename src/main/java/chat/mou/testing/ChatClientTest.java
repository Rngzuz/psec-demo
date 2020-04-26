package chat.mou.testing;

import chat.mou.ChatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ChatClientTest
{
    public static void main(String[] args)
    {
        /*final var executor = Executors.newSingleThreadExecutor();
        final var connectAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);

        try (final var chatClient = new ChatClient(connectAddress)) {
            executor.submit(chatClient);

            final var reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                final var message = reader.readLine();

                if (message.equals("-S")) {
                    chatClient.close();
                    break;
                }

                chatClient.sendMessage(message.getBytes());
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        finally {
            executor.shutdownNow();
        }*/
    }
}
