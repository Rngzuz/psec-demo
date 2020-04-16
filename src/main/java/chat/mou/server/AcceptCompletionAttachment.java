package chat.mou.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;

public class AcceptCompletionAttachment {
    private final AsynchronousServerSocketChannel serverChannel;
    private final Map<String, ClientSession> addressToSessionMap;

    public AcceptCompletionAttachment(AsynchronousServerSocketChannel serverChannel, Map<String, ClientSession> addressToSessionMap) {
        this.serverChannel = serverChannel;
        this.addressToSessionMap = addressToSessionMap;
    }

    public AsynchronousServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public Map<String, ClientSession> getAddressToSessionMap() {
        return addressToSessionMap;
    }
}
