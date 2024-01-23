package http.handler;

import http.config.Configuration;
import http.config.HttpConfig;
import http.messages.HttpRequest;
import http.resolver.Resolver;
import http.resolver.ResolverMaster;
import http.util.HttpUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Deprecated
public class ReadHandler implements Runnable {

    private SelectionKey key;
    private ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
    private HttpRequest httpRequestHeader;
    private HttpConfig httpConfig;
    private ResolverMaster resolverMaster;
    public ReadHandler(SelectionKey key, ResolverMaster resolverMaster) {
        this.key = key;
        this.resolverMaster = resolverMaster;
        this.httpConfig = HttpConfig.getInstance();
    }

    @Override
    public void run() {

        // 버퍼 초기화
        buffer.clear();
        SocketChannel client = (SocketChannel) key.channel();
        int read;
        HttpRequest httpRequest = null;

        try {
            synchronized (client) {
                if (!client.isOpen()) {
                    System.out.println("client is already closed");
                    return;
                }

                httpRequest = getHttpRequest(client);

                if (httpRequest != null) {
                    System.out.println("httpRequest = " + httpRequest.toString());

                    if (httpRequest.getPath().contains("favicon")) {
                        favicon(httpRequest, client);
                    }
                    handleHttpRequest(httpRequest, client);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.closeSocket(client);
        }

    }

    private void favicon(HttpRequest httpRequest, SocketChannel client) throws IOException {
        String path = httpRequest.getPath().toLowerCase();
        Resolver resolver = resolverMaster.getResolver(path);
        byte[] bodyByte = resolver.handle(httpRequest);

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: image/png\r\n" +
                "Content-Length: " + bodyByte.length + "\r\n" +
                "\r\n";

        // 전송
        sendResponse(httpRequest, client, httpResponse);
        sendResponseBody(httpRequest, client, bodyByte);
    }

    private HttpRequest getHttpRequest(SocketChannel client) {
        int byteRead = 0;

        try {
            if (!client.isOpen()) {
                return null;
            }
            byteRead = client.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        HttpRequest httpRequest = null;

        if (byteRead == -1) {
            HttpUtils.closeSocket(client);
            return null;
        }

        if (byteRead > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            httpRequest = HttpRequest.Builder.buildByByteArray(bytes);
        }

        return httpRequest;
    }

    private void handleHttpRequest(HttpRequest httpRequest, SocketChannel client) throws IOException {

        String path = httpRequest.getPath().toLowerCase();

        Resolver resolver = resolverMaster.getResolver(path);
        byte[] bodyByte = resolver.handle(httpRequest);
        String body = new String(bodyByte);

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body;

        // 전송
        sendResponse(httpRequest, client, httpResponse);
    }

    private void sendResponse(HttpRequest httpRequest, SocketChannel client, String httpResponse) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.wrap(httpResponse.getBytes());

        while (responseBuffer.hasRemaining()) {
            client.write(responseBuffer);
        }

        // Keep the connection alive for HTTP/1.1 (optional)
        if (!httpRequest.isConnectionOptionIsClose()) {
            key.interestOps(SelectionKey.OP_READ);
        } else {
            HttpUtils.closeSocket(client);
        }
    }

    private void sendResponseBody(HttpRequest httpRequest, SocketChannel client, byte[] body) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.wrap(body);

        while (responseBuffer.hasRemaining()) {
            client.write(responseBuffer);
        }

        // Keep the connection alive for HTTP/1.1 (optional)
        if (!httpRequest.isConnectionOptionIsClose()) {
            key.interestOps(SelectionKey.OP_READ);
        } else {
            HttpUtils.closeSocket(client);
        }
    }

    private static String getDefaultResponse(HttpRequest httpRequest) {
        int[] emoji = {0xD83C, 0xDF89};
        String emojiString = new String(emoji, 0, emoji.length);
        System.out.println("emojiString = " + emojiString);
        String body = "<textarea style=\"width:100%; height:200px\" datatype=\"varchar(4000)\" readonly=\"true\">" + httpRequest.getBody() + " return this! " + emojiString + "hello! </>";
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body;
        return httpResponse;
    }
}
