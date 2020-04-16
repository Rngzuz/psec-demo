package chat.mou.server;

import java.io.*;

public class Message implements Serializable {
    public enum Action {
        MESSAGE,
        CONNECT,
        DISCONNECT
    }

    public final String displayName;
    public final Action action;
    public final String body;

    public Message(String displayName, Action action, String body) {
        this.displayName = displayName;
        this.action = action;
        this.body = body;
    }

    public Message(byte [] message) throws IOException, ClassNotFoundException {
        final var object = deserialize(message);

        this.displayName = object.displayName;
        this.action = object.action;
        this.body = object.body;
    }

    public byte[] serialize(Message message) throws IOException {
        // Write the object to an in-memory stream and get resulting bytes
        final var stream = new ByteArrayOutputStream();
        final var objectStream = new ObjectOutputStream(stream);

        objectStream.writeObject(message);

        return stream.toByteArray();
    }

    private static Message deserialize(byte[] message) throws IOException, ClassNotFoundException {
        final var stream = new ByteArrayInputStream(message);
        final var objectStream = new ObjectInputStream(stream);

        return (Message) objectStream.readObject();
    }
}
