package chat.mou.client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws Exception {
        final var client = SocketChannel.open();
        client.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080));

        final var input = new DataInputStream(client.socket().getInputStream());
        final var output = new DataOutputStream(client.socket().getOutputStream());

        // Input message from user
        final var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            final var userMessage = reader.readLine();
            output.writeUTF(userMessage);
            output.flush();

            System.out.println(input.readUTF());

            if (userMessage.equals("-S")) {
                break;
            }
        }

        client.close();
    }
}
