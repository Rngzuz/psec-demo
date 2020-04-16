package chat.mou.client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws Exception {
//        final var executor = Executors.newSingleThreadExecutor();
//        executor.submit(new ChatServer(8080));
    }

//    public static void main(String[] args) throws IOException {
//        final var messaging = new ServerSocket();
//        messaging.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080));
//
//        final var interface = messaging.accept();
//
//        final var input = new DataInputStream(interface.getInputStream());
//        final var output = new DataOutputStream(interface.getOutputStream());
//
//        // Input message from user
//        final var reader = new BufferedReader(new InputStreamReader(System.in));
//
//        while (true) {
//
//
//            final var inputMessage = input.readUTF();
//            System.out.println(inputMessage);
//
//            interface.getOutputStream().write(interface.getInputStream().readAllBytes());
//            interface.getOutputStream().flush();
//
//            if (inputMessage.equals("-S")) {
//                break;
//            }
//        }
//
//        interface.close();
//        messaging.close();
//    }
}
