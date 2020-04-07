package chat.mou.messaging;

import sun.misc.Signal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;

public class ChatHostStart {
    public static void main(String[] args) {
        startChatHost(8080);
    }

    public static void startChatHost(int port) {
        final var hostAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);

        try (final var chatHost = new ChatHost(hostAddress)) {
            /* trap the INT (CTRL+C) signal */
            Signal.handle(new Signal("INT"), signal -> {
                try {
                    chatHost.close();
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
                finally {
                    System.out.println("Server stopped.");
                    System.exit(0);
                }
            });

            chatHost.open();

            while (!Thread.interrupted()) {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch (UnresolvedAddressException exception) {
            System.out.println("Cannot resolve the specified address");
            exception.printStackTrace();
            System.exit(1);
        }
        catch (SocketException exception) {
            System.out.println("Specified port is in use already");
            exception.printStackTrace();
            System.exit(1);
        }
        catch (Exception exception) {
            System.out.println("Other messaging error");
            exception.printStackTrace();
        }
        finally {
            System.out.println("Terminating the messaging...");
        }
    }
}