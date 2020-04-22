package chat.mou.testing;

import chat.mou.client.Client;
import chat.mou.shared.EventType;
import chat.mou.shared.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ClientTest
{
    public static void main(String[] args)
    {
        final var executor = Executors.newSingleThreadExecutor();
        final var connectAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);

        try (final var client = new Client(connectAddress)) {
            final var eventBus = client.getEventBus();

            eventBus.addListener(EventType.BROADCAST, message -> {
                System.out.println(message.getBody());
            });

            executor.submit(client);

            final var reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                final var userMessage = reader.readLine();

                if (userMessage.equals("-S")) {
                    eventBus.dispatch(new Message(EventType.DISCONNECT, null));
                    client.close();
                    break;
                }
                else {
                    eventBus.dispatch(new Message(EventType.WRITE, userMessage));
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        finally {
            //executor.shutdownNow();
        }
    }
}
