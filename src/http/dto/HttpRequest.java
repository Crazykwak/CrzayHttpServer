package http.dto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private HttpMethod method;
    private String path;
    private String httpVersion;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private String body;

    @Override
    public String toString() {
        return "method : " + method +
                "\npath : " + path +
                "\nhttpVersion : " + httpVersion +
                "\nheaders : " + headers.toString() +
                "\ncookies : " + cookies.toString() +
                "\nbody : " + body;
    }

    // private 생성자로 외부에서 직접 객체 생성을 막고, 빌더를 통해 생성하도록 합니다.
    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.httpVersion = builder.httpVersion;
        this.headers = builder.headers;
        this.cookies = builder.cookies;
        this.body = builder.body;
    }

    private HttpRequest(HttpMethod method, String path, String httpVersion, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean isConnectionOptionIsClose() {
        return headers.get("Connection").equals("close");
    }

    public static class Builder {

        private HttpMethod method;
        private String path;
        private String httpVersion;
        private Map<String, String> headers;
        private Map<String, String> cookies;
        private String body;

        public Builder() {
        }

        // 필수 매개변수를 받는 생성자
        public Builder(HttpMethod method, String path, String httpVersion) {
            this.method = method;
            this.path = path;
            this.httpVersion = httpVersion;
            this.headers = new HashMap<>();
        }

        public static HttpRequest buildByByteArray(byte[] input) {
            String httpRequestString = new String(input);

            String[] requestLines = httpRequestString.split("\\r?\\n");

            if (requestLines.length < 0) {
                throw new RuntimeException("Wrong Http Request. Http message is null");
            }

            String[] firstLine = requestLines[0].split("\\s+");

            if (firstLine.length != 3) {
                throw new RuntimeException("Wrong Http Request. Http message's first line must be [Method, Path, Version]");
            }

            HttpMethod method = HttpMethod.valueOf(firstLine[0]);
            String path = firstLine[1];
            String httpVersion = firstLine[2];
            Map<String, String> headers = new HashMap<>();
            Map<String, String> cookies = new HashMap<>();

            boolean bodyFlag = false;
            StringBuilder body = new StringBuilder();

            for (int i = 1; i < requestLines.length; i++) {
                String headerLine = requestLines[i];

                if (bodyFlag) {
                    body.append(headerLine);
                }

                String[] headerParts = headerLine.split(":\\s", 2);

                if (headerParts.length < 2) {
                    bodyFlag = true;
                }

                if (headerParts.length == 2) {
                    if (headerParts[0].equals("Cookie")) {
                        setCookie(headerParts[1], cookies);
                        continue;
                    }
                    headers.put(headerParts[0], headerParts[1]);
                }
            }

            return new HttpRequest.Builder(method, path, httpVersion)
                    .addHeader(headers)
                    .addCookies(cookies)
                    .setBody(body.toString())
                    .build();
        }

        private static void setCookie(String cookieParts, Map<String, String> cookiesMap) {
            String[] cookies = cookieParts.split(";");
            for (String cookie : cookies) {
                String[] split = cookie.split("=");
                cookiesMap.put(split[0], split[1]);
            }
        }

        // 선택적 매개변수들을 빌더에 추가하는 메서드들
        public Builder addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder addHeader(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder addCookies(Map<String, String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder setHttpVersion(String httpVersion) {
            this.httpVersion = httpVersion;
            return this;
        }

        // 빌더 패턴의 핵심 메서드
        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
