package http.resolver;

import http.config.HttpConfig;
import http.messages.HttpRequest;
import http.messages.HttpResponse;
import http.messages.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DefaultResolver implements Resolver {

    protected HttpConfig httpConfig;
    protected File notFound;
    protected final static String defaultNotFound = "./static/notFound.html";

    public DefaultResolver(String notFound) {
        this.httpConfig = HttpConfig.getInstance();

        File file = new File(notFound);
        if (file.exists() && file.isFile()) {
            this.notFound = file;
        } else {
            System.out.println("notFound File is not found. use default file");
        }
    }

    public DefaultResolver() {
        this.httpConfig = HttpConfig.getInstance();
        this.notFound = new File(defaultNotFound);
    }

    @Override
    public byte[] handle(HttpRequest httpRequest) {
        String path = httpRequest.getPath();
        File targetFile = findTargetFile(path);

        try {
            byte[] body = Files.readAllBytes(targetFile.toPath());
            String contentType = determineContentType(targetFile);
            Map<String, String> headers = makeHeader(body.length, contentType);

            // 응답 헤더 설정
            HttpResponse httpResponse = new HttpResponse.Builder()
                    .setStatus(HttpStatus.OK)
                    .setHeaders(headers)
                    .setBody(body)
                    .build();

            return httpResponse.getHttpResponseBytes();
        } catch (IOException e) {
            // 파일을 읽어오지 못한 경우 404 응답 생성
            return createNotFoundResponse().getHttpResponseBytes();
        }
    }

    private Map<String, String> makeHeader(int length, String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", String.valueOf(length));
        headers.put("Content-Type", contentType);

        return headers;
    }

    private File findTargetFile(String path) {

        File targetFile = new File(httpConfig.getRootPath() + path);

        return targetFile;
    }

    private String determineContentType(File file) {
        // 파일 확장자를 기반으로 Content-Type을 결정
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

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
            default:
                return "application/octet-stream";
        }
    }

    private HttpResponse createNotFoundResponse() {
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(notFound.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HttpResponse.Builder()
                .setStatus(HttpStatus.NOT_FOUND)
                .setBody(bytes)  // 빈 바디 설정
                .build();
    }
}
