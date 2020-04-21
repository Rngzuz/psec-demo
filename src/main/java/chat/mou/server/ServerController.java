package chat.mou.server;

import chat.mou.shared.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class ServerController
{
    private final Map<String, Session> sessionMap;

    public ServerController()
    {
        sessionMap = new ConcurrentHashMap<>();
    }

    public void putSession(Session session)
    {
        sessionMap.put(session.getAddress(), session);
    }

    public void removeSession(Session session)
    {
        sessionMap.remove(session.getAddress());
    }

    public void broadcastMessage(Message message)
    {
        try {
            final var messageBytes = message.serialize();

            // Loop through sessions and send the message
            for (final var address : sessionMap.keySet()) {
                final var session = sessionMap.get(address);

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
    }
}
