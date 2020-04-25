package chat.mou.testing;

import chat.mou.server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerTest
{
    public static void main(String[] args)
    {
        final var executor = Executors.newSingleThreadExecutor();
        final var bindAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);

        try (final var server = new Server(bindAddress)) {
            executor.submit(server);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        finally {
            executor.shutdownNow();
        }
    }

}
