package http.handler;

import http.config.Configuration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static http.handler.HandlerState.READING;
import static http.handler.HandlerState.SENDING;

public class SocketHandler implements Handler{

    private SocketChannel client;
    private SelectionKey selectionKey;
    private int state = READING;
    private final Queue<ByteBuffer> bufferQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService workerPool = Executors.newFixedThreadPool(Configuration.WORKER_SIZE);

    public SocketHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.client = socketChannel;
        this.client.configureBlocking(false);

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
        workerPool.execute(new Worker(client, selectionKey, bufferQueue, SENDING));
        turnReadMod();
    }

    private void turnReadMod() {
        if (!selectionKey.isValid()) {
            // key is cancelled. do nothing
            return;
        }
        selectionKey.interestOps(SelectionKey.OP_READ);
        state = READING;
    }

    private void read() throws IOException {
        workerPool.execute(new Worker(client, selectionKey, bufferQueue, READING));
        turnWriteMod();
    }

    private void turnWriteMod() {
        if (!selectionKey.isValid()) {
            // key is cancelled. do nothing
            return;
        }

        selectionKey.interestOps(SelectionKey.OP_WRITE);
        state = SENDING;
    }
}
