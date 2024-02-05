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
    private final ExecutorService workerPool = Executors.newFixedThreadPool(Configuration.WORKER_SIZE);

    public SocketHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.client = socketChannel;
        this.client.configureBlocking(false);

        socketChannel.socket().setTcpNoDelay(true);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    @Override
    public void handle() {
        synchronized (client) {
            if (!client.isOpen()) {
                return;
            }
            executeWorker();
        }
    }

    private void executeWorker() {
        workerPool.execute(new Worker(client));
    }
}
