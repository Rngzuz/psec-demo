package chat.mou.testing;

import chat.mou.server.Server;

import java.io.IOException;
import java.util.concurrent.Executors;

public class ServerTest
{
    public static void main(String[] args)
    {
        final var executor = Executors.newSingleThreadExecutor();

        try (final var server = new Server()) {
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
