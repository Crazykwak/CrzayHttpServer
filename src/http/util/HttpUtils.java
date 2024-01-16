package http.util;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

public class HttpUtils {

    public static void closeSocket(SocketChannel client) {
        System.out.println("socket close. ip : " + client.socket().getInetAddress().getHostAddress());
        try {
            client.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
