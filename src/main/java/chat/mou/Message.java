package chat.mou;

import java.io.*;

public class Message implements Serializable
{
    public enum Type
    {
        KEY,
        TEXT
    }

    private final Type type;
    private final byte[] data;

    public Message(Type type, byte[] data)
    {
        this.type = type;
        this.data = data;
    }

    public Type getType()
    {
        return type;
    }

    public byte[] getData()
    {
        return data;
    }

    public static byte[] serialize(Message message) throws IOException
    {
        // Write the object to an in-memory stream and get resulting bytes
        final var stream = new ByteArrayOutputStream();
        final var objectStream = new ObjectOutputStream(stream);

        objectStream.writeObject(message);

        return stream.toByteArray();
    }

    public static Message deserialize(byte[] message) throws IOException, ClassNotFoundException
    {
        final var stream = new ByteArrayInputStream(message);
        final var objectStream = new ObjectInputStream(stream);

        return (Message) objectStream.readObject();
    }
}
