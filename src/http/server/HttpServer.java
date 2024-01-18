package http.server;

import http.config.Configuration;
import http.handler.ReadHandler;
import http.resolver.FreemarkerResolver;
import http.resolver.ResolverMaster;
import http.servlet.DefaultServlet;
import http.util.HttpUtils;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer extends Thread{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ServerSocket serverSocket;
    private ExecutorService readHandlerPool = Executors.newFixedThreadPool(Configuration.WORKER_SIZE);
    private HttpServlet httpServlet;

    public HttpServer(int port, HttpServlet httpServlet) {
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocket = serverSocketChannel.socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            serverSocketChannel.socket().setSoTimeout(10000);

            serverSocket.bind(inetSocketAddress);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            this.httpServlet = httpServlet;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpServer() {
        this(8080, new DefaultServlet());
    }

    @Override
    public void run() {
        SelectionKey key = null;

        while (true) {
            try {
                int select = selector.select();

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if (key.channel().isOpen() && key.isAcceptable()) {
                        acceptableHandle(key);
                    }
                    if (key.channel().isOpen() && key.isReadable()) {
                        readableHandle(key);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                HttpUtils.closeSocket((SocketChannel) key.channel());
            } catch (CancelledKeyException e) {
                e.printStackTrace();
            }
        }

    }

    private void readableHandle(SelectionKey key) throws IOException {
        readHandlerPool.execute(new ReadHandler(key, ResolverMaster.getInstance()));
    }

    private void acceptableHandle(SelectionKey key) throws IOException {
        try {
            SocketChannel client = serverSocketChannel.accept();
            if (client == null) {
                System.out.println("client is null. What The FUCK");
                return;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("new Client is Accept");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
