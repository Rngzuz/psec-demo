package chat.mou.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class ReadCompletionAttachment {
    private final AsynchronousSocketChannel channel;
    private final Map<String, ClientSession> addressToSessionMap;
    private final ByteBuffer inputBuffer;

    public ReadCompletionAttachment(AsynchronousSocketChannel channel, Map<String, ClientSession> addressToSessionMap, ByteBuffer inputBuffer) {
        this.channel = channel;
        this.addressToSessionMap = addressToSessionMap;
        this.inputBuffer = inputBuffer;
    }

    public AsynchronousSocketChannel getChannel() {
        return channel;
    }

    public Map<String, ClientSession> getAddressToSessionMap() {
        return addressToSessionMap;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }
}
