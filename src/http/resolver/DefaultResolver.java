package http.resolver;

import http.config.HttpConfig;
import http.messages.HttpRequest;
import http.messages.HttpResponse;
import http.messages.HttpStatus;
import http.util.HttpUtils;

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
            String contentType = HttpUtils.determineContentType(targetFile);
            Map<String, String> headers = HttpUtils.makeHeader(body.length, contentType);

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



    private File findTargetFile(String path) {

        File targetFile = new File(httpConfig.getRootPath() + path);

        return targetFile;
    }

    private HttpResponse createNotFoundResponse() {
        byte[] body = new byte[0];
        Map<String, String> headers;
        try {
            body = Files.readAllBytes(notFound.toPath());
            String contentType = HttpUtils.determineContentType(notFound);
            headers = HttpUtils.makeHeader(body.length, contentType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HttpResponse.Builder()
                .setStatus(HttpStatus.NOT_FOUND)
                .setHeaders(headers)
                .setBody(body)  // 빈 바디 설정
                .build();
    }
}
