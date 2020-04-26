package chat.mou.network;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SocketConnectionManager
{
    private final ClientSocketConnection clientSocketConnection;
    private final HostSocketConnection hostSocketConnection;

    public SocketConnectionManager(ClientSocketConnection clientSocketConnection, HostSocketConnection hostSocketConnection)
    {
        this.clientSocketConnection = clientSocketConnection;
        this.hostSocketConnection = hostSocketConnection;
    }
}
