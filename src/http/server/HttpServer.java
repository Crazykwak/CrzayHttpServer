package http.server;

import http.handler.AcceptHandler;
import http.handler.Handler;
import http.monitor.TimeoutMonitor;
import http.servlet.DefaultServlet;
import http.util.HttpUtils;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HttpServer extends Thread{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ServerSocket serverSocket;
    private Map<SelectionKey, Long> timeoutMap = new ConcurrentHashMap<>();
    private HttpServlet httpServlet;

    public HttpServer(int port, HttpServlet httpServlet) {
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocket = serverSocketChannel.socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            serverSocketChannel.socket().setSoTimeout(10000);
            serverSocket.bind(inetSocketAddress, 200);
            this.serverSocketChannel.configureBlocking(false);

            SelectionKey serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverKey.attach(new AcceptHandler(selector, serverSocketChannel));

            this.httpServlet = httpServlet;

            Thread timeoutMonitor = new TimeoutMonitor(timeoutMap, serverSocketChannel);
            timeoutMonitor.start();

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
                    addTimeoutMap(selectionKey);
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

    private void addTimeoutMap(SelectionKey selectionKey) {
        this.timeoutMap.put(selectionKey, System.currentTimeMillis());
    }


    private void dispatch(SelectionKey key) {

        Handler attachment = (Handler) key.attachment();
        attachment.handle();

    }
}
