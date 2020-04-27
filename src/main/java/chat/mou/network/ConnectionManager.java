package chat.mou.network;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope("singleton")
public class ConnectionManager
{
    private SocketConnection socketConnection;
    private ExecutorService executor;

    public SocketConnection getSocketConnection()
    {
        return socketConnection;
    }

    public void setSocketConnection(SocketConnection socketConnection)
    {
        this.socketConnection = socketConnection;
    }

    public boolean openSocketConnection(InetSocketAddress address)
    {
        if (!socketConnection.isOpen()) {
            socketConnection.setAddress(address);

            executor = Executors.newSingleThreadExecutor();
            executor.submit(socketConnection);
        }

        return socketConnection.isOpen();
    }

    public boolean closeSocketConnection()
    {
        if (executor != null) {
            try {
                if (socketConnection != null && socketConnection.isOpen()) {
                    socketConnection.close();
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            finally {
                executor.shutdownNow();
            }
        }

        return socketConnection.isOpen();
    }

    public boolean isOpen()
    {
        return socketConnection != null && socketConnection.isOpen();
    }

    public boolean isHost()
    {
        return socketConnection != null && socketConnection.isHost();
    }
}
