package http.resolver;

import http.config.HttpConfig;
import http.messages.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DefaultResolver implements Resolver {

    private HttpConfig httpConfig;
    private File notFound;
    private final static String defaultNotFound = "./static/notFound.html";

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
        byte[] body = new byte[0];

        File targetFile = findTargetFile(path);
        try {
            body = Files.readAllBytes(targetFile.toPath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return body;
    }

    private File findTargetFile(String path) {

        File targetFile = new File(httpConfig.getRootPath() + path);
        if (targetFile.exists() && targetFile.isFile()) {
            return targetFile;
        }

        return notFound;
    }
}
