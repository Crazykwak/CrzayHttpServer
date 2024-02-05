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
import java.util.Queue;

import static http.handler.HandlerState.READING;
import static http.handler.HandlerState.SENDING;

public class Worker implements Runnable {

    private SocketChannel client;
    private final ResolverMaster resolverMaster;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);

    public Worker(SocketChannel client) {
        this.client = client;
        this.resolverMaster = ResolverMaster.getInstance();
    }

    @Override
    public void run() {
        synchronized (client) {
            read();
            try {
                send();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void read() {
        HttpRequest httpRequest = getHttpRequest(byteBuffer);
        byteBuffer.clear();
        if (httpRequest == null) {
            return;
        }
        byte[] responseByte = handleHttpRequest(httpRequest);
        byteBuffer.put(responseByte);


    }

    private void send() throws IOException {
        if (byteBuffer == null) {
            return;
        }
        byteBuffer.flip();
        while (byteBuffer.hasRemaining()) {
            client.write(byteBuffer);
        }
        byteBuffer.clear();
    }

    private HttpRequest getHttpRequest(ByteBuffer readBuffer) {
        int byteRead = 0;

        try {
            if (!client.isOpen()) {
                return null;
            }
            byteRead = client.read(readBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            HttpUtils.closeSocket(client);
            return null;
        }
        HttpRequest httpRequest = null;

        if (byteRead == -1) {
            HttpUtils.closeSocket(client);
            return null;
        }

        if (byteRead > 0) {
            readBuffer.flip();
            byte[] bytes = new byte[readBuffer.remaining()];
            readBuffer.get(bytes);
            httpRequest = HttpRequest.Builder.buildByByteArray(bytes);
        }

        return httpRequest;
    }

    private byte[] handleHttpRequest(HttpRequest httpRequest) {

        String path = httpRequest.getPath().toLowerCase();

        Resolver resolver = resolverMaster.getResolver(path);
        byte[] bodyByte = resolver.handle(httpRequest);

        return bodyByte;
    }
}
