package http.messages;

import java.util.Map;

public class HttpProtocol {
    protected HttpMethod method;
    protected String path;
    protected String httpVersion;
    protected Map<String, String> headers;
    protected Map<String, String> cookies;
    protected String body;
}
