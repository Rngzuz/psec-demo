//package chat.mou.testing;
//
//import chat.mou.ChatServer;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.util.concurrent.Executors;
//
//public class ChatServerTest
//{
//    public static void main(String[] args)
//    {
//        final var executor = Executors.newSingleThreadExecutor();
//        final var bindAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);
//
//        try (final var chatServer = new ChatServer(bindAddress)) {
//            executor.submit(chatServer);
//
//            final var reader = new BufferedReader(new InputStreamReader(System.in));
//
//            while (true) {
//                final var message = reader.readLine();
//
//                if (message.equals("-S")) {
//                    chatServer.close();
//                    break;
//                }
//
//                chatServer.sendMessage(message.getBytes());
//            }
//        }
//        catch (IOException exception) {
//            exception.printStackTrace();
//        }
//        finally {
//            executor.shutdownNow();
//        }
//    }
//}
