package http.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpConfig {

    private static HttpConfig httpConfig;
    private String rootPath;
    private File favicon;

    private HttpConfig(String rootPath) {
        this.rootPath = rootPath;
        this.favicon = new File("./static/favicon.png");
    }

    public String getRootPath() {
        return rootPath;
    }

    public File getFavicon() {
        return favicon;
    }

    public synchronized static HttpConfig getInstance() {
        if (httpConfig == null) {
            httpConfig = new HttpConfig("./webapp");
        }
        return httpConfig;
    }
}
