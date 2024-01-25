package http.util;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static void closeSocket(SocketChannel client) {
        try {
            synchronized (client) {
                client.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, String> makeHeader(int length, String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", String.valueOf(length));
        headers.put("Content-Type", contentType);

        return headers;
    }

    public static String determineContentType(File file) {
        // 파일 확장자를 기반으로 Content-Type을 결정
        String fileName = file.getName();
        return determineContentType(fileName);
    }

    public static String determineContentType(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "html":
                return "text/html; charset=" + Charset.defaultCharset().name();
            case "css":
                return "text/css; charset=" + Charset.defaultCharset().name();
            case "js":
                return "application/javascript; charset=" + Charset.defaultCharset().name();
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "ftl":
                return "application/json; charset=" + Charset.defaultCharset().name();
            default:
                return "application/octet-stream";
        }
    }
}
