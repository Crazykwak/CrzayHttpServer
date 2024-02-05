package http.monitor;

import http.config.HttpConfig;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class TimeoutMonitor extends Thread {

    private final HttpConfig httpConfig = HttpConfig.getInstance();
    private final Map<SelectionKey, Long> timeoutMap;
    private long currentTime = System.currentTimeMillis();
    private final ServerSocketChannel serverSocketChannel;

    public TimeoutMonitor(Map<SelectionKey, Long> timeoutMap, ServerSocketChannel serverSocketChannel) {
        this.timeoutMap = timeoutMap;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {

        while (true) {
            try {
                currentTime = System.currentTimeMillis();
                for (Map.Entry<SelectionKey, Long> entry : timeoutMap.entrySet()) {
                    long lastTime = entry.getValue();
                    SelectionKey key = entry.getKey();
                    SelectableChannel channel = key.channel();

                    if (currentTime > lastTime + httpConfig.getKeepAliveTimeOut() && channel.isOpen()) {
                        channel.close();
                        key.cancel();
                    }
                }
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
