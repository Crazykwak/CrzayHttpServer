package http.handler;

import http.config.Configuration;
import http.messages.HttpRequest;
import http.resolver.Resolver;
import http.resolver.ResolverMaster;
import http.util.HttpUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketHandler implements Handler{

    private static final int READING = 0, SENDING = 1;
    private SocketChannel client;
    private SelectionKey selectionKey;
    private int state = READING;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
    private final ResolverMaster resolverMaster;

    public SocketHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.client = socketChannel;
        this.client.configureBlocking(false);
        this.resolverMaster = ResolverMaster.getInstance();

        socketChannel.socket().setTcpNoDelay(true);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    @Override
    public synchronized void handle() {
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

    private void send() throws IOException {
        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            client.write(writeBuffer);
        }
        writeBuffer.clear();
        selectionKey.interestOps(SelectionKey.OP_READ);
        state = READING;
    }

    private void read() throws IOException {

        HttpRequest httpRequest = getHttpRequest();
        readBuffer.clear();
        if (httpRequest == null) {
            return;
        }
        byte[] responseByte = handleHttpRequest(httpRequest);
        writeBuffer.put(responseByte);

        selectionKey.interestOps(SelectionKey.OP_WRITE);
        state = SENDING;
    }

    private HttpRequest getHttpRequest() {
        int byteRead = 0;

        try {
            if (!client.isOpen()) {
                return null;
            }
            byteRead = client.read(readBuffer);
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
