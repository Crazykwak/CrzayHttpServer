package http.server;

import http.config.Configuration;
import http.handler.AcceptHandler;
import http.handler.HandlePool;
import http.handler.Handler;
import http.handler.ReadHandler;
import http.resolver.ResolverMaster;
import http.servlet.DefaultServlet;
import http.util.HttpUtils;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
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
            this.serverSocket = serverSocketChannel.socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            serverSocketChannel.socket().setSoTimeout(10000);
            serverSocket.bind(inetSocketAddress);
            this.serverSocketChannel.configureBlocking(false);

            SelectionKey serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverKey.attach(new AcceptHandler(selector, serverSocketChannel));

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

                if (select < 0) {
                    System.out.println("no select. continue");
                    continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    dispatch(selectionKey);
                }
                selectionKeys.clear();

            } catch (IOException e) {
                e.printStackTrace();
                HttpUtils.closeSocket((SocketChannel) key.channel());
            } catch (CancelledKeyException e) {
                e.printStackTrace();
            }
        }

    }

    private void dispatch(SelectionKey key) {
        readHandlerPool.execute(new HandlePool(key));
    }

    @Deprecated
    private void readableHandle(SelectionKey key) throws IOException {
        readHandlerPool.execute(new ReadHandler(key, ResolverMaster.getInstance()));
    }

    @Deprecated
    private void acceptableHandle(SelectionKey key) throws IOException {
        try {
            SocketChannel client = serverSocketChannel.accept();
            Socket socket = client.socket();
            socket.setTcpNoDelay(true);
            if (client == null) {
                System.out.println("client is null. What The FUCK");
                return;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
