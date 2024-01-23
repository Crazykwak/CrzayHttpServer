package http.handler;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class HandlePool implements Runnable{


    private SelectionKey key;

    public HandlePool(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void run() {

        Handler attachment = (Handler) key.attachment();
        attachment.handle();

    }
}
