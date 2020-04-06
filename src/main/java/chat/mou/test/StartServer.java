package chat.mou.test;

import sun.misc.Signal;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;

public class StartServer {
    public StartServer(int port) {
        try (Server server = new Server(8080)){

            /* trap the INT (CTRL+C) signal */
            Signal.handle(
                new Signal("INT"),
                signal -> {
                    try {
                        server.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        System.out.println("Server stopped.");
                        System.exit(0);
                    }
                }
            );

            while (!Thread.interrupted()) {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch (UnresolvedAddressException ua) {
            System.out.println("Cannot resolve the specified address");
            System.exit(1);
        }
        catch (SocketException se) {
            System.out.println("Specified port is in use already");
            System.exit(1);
        }
        catch (Exception e) {
            System.out.println("Other server error");
        }
        finally {
            System.out.println("Terminating the server...");
        }
    }

    public static void main(String[] args) {
        new StartServer(8080);
    }
}