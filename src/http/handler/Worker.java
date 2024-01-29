package http.handler;

import http.config.Configuration;
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
    private final Queue<ByteBuffer> bufferQueue;
    private int state;

    public Worker(SocketChannel client, Queue<ByteBuffer> bufferQueue, int state) {
        this.client = client;
        this.bufferQueue = bufferQueue;
        this.resolverMaster = ResolverMaster.getInstance();
        this.state = state;
    }

    @Override
    public void run() {

        try {
            synchronized (client) {
                if (state == READING) {

                    read();

                    return;
                }

                if (state == SENDING) {
                    send();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void read() {
        ByteBuffer readBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
        HttpRequest httpRequest = getHttpRequest(readBuffer);
        readBuffer.clear();
        if (httpRequest == null) {
            return;
        }
        byte[] responseByte = handleHttpRequest(httpRequest);
        readBuffer.put(responseByte);
        bufferQueue.add(readBuffer);
    }

    private void send() throws IOException {
        ByteBuffer writeBuffer = bufferQueue.poll();
        if (writeBuffer == null) {
            return;
        }
        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            client.write(writeBuffer);
        }
        writeBuffer.clear();
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
