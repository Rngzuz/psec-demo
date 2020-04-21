package chat.mou.shared;

import java.io.*;

public final class Message implements Serializable {
    private final Action action;
    private final String body;

    public Message(Action action, String body) {
        this.action = action;
        this.body = body;
    }

    public Action getAction()
    {
        return action;
    }

    public String getBody()
    {
        return body;
    }

    public byte[] serialize() throws IOException {
        // Write the object to an in-memory stream and get resulting bytes
        final var stream = new ByteArrayOutputStream();
        final var objectStream = new ObjectOutputStream(stream);

        objectStream.writeObject(this);

        return stream.toByteArray();
    }

    public static Message deserialize(byte[] message) {
        try {
            final var stream = new ByteArrayInputStream(message);
            final var objectStream = new ObjectInputStream(stream);

            return (Message) objectStream.readObject();
        }
        catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return new Message(Action.ERROR, "");
    }
}
