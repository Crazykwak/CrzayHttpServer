package http.handler;

import http.DBFactory;
import http.config.Configuration;
import http.dto.HttpRequest;
import http.util.HttpUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadHandler implements Runnable {

    private SelectionKey key;
    private ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
    private HttpRequest httpRequestHeader;

    public ReadHandler(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void run() {

        // 버퍼 초기화
        buffer.clear();
        SocketChannel client = (SocketChannel) key.channel();
        int read;
        HttpRequest httpRequest = null;

        try {
            if (!client.isOpen()) {
                return;
            }

            httpRequest = getHttpRequest(client);

            if (httpRequest != null) {
                handleHttpRequest(httpRequest, client);
            }

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.closeSocket(client);
        }

    }

    private HttpRequest getHttpRequest(SocketChannel client) throws IOException {
        int byteRead = client.read(buffer);
        HttpRequest httpRequest = null;

        if (byteRead == -1) {
            HttpUtils.closeSocket(client);
            return null;
        }

        if (byteRead > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            httpRequest = HttpRequest.Builder.buildByByteArray(bytes);
            System.out.println("httpRequest = " + httpRequest);
        }

        return httpRequest;
    }

    private void handleHttpRequest(HttpRequest httpRequest, SocketChannel client) throws IOException {

        String result = null;
        try {

            String query = "select * from emoji";
            result = DBFactory.executeQuery(query);
            System.out.println("result = " + result);

            for (int i = 0; i < result.length(); i++) {
                int codePoint = result.codePointAt(i);
                System.out.println("Unicode Code Point: U+" + Integer.toHexString(codePoint).toUpperCase());

                // Increment i by the number of chars in this code point
                i += Character.charCount(codePoint);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 무적권 이거 응답
        int[] emoji = {0xD83C, 0xDF89};
        String emojiString = new String(emoji, 0, emoji.length);
        System.out.println("emojiString = " + emojiString);
        String body = "<textarea style=\"width:100%; height:200px\" datatype=\"varchar(4000)\" readonly=\"true\">" + httpRequest.getBody() + " return this! " + emojiString + result + "hello! </>";
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=euc-kr\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body;


        // 전송
        ByteBuffer responseBuffer = ByteBuffer.wrap(httpResponse.getBytes());
        while (responseBuffer.hasRemaining()) {
            client.write(responseBuffer);
        }

        System.out.println("httpResponse = " + httpResponse);

        // Keep the connection alive for HTTP/1.1 (optional)
        if (!httpRequest.isConnectionOptionIsClose()) {
            key.interestOps(SelectionKey.OP_READ);
        } else {
            client.close();
        }
    }
}
