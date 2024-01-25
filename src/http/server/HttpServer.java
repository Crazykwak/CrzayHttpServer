package http.server;

import http.handler.AcceptHandler;
import http.handler.Handler;
import http.servlet.DefaultServlet;
import http.util.HttpUtils;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.Set;

public class HttpServer extends Thread{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ServerSocket serverSocket;
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

        Handler attachment = (Handler) key.attachment();
        attachment.handle();

    }
}
