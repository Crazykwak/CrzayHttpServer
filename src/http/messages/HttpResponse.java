package http.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private HttpStatus status;
    private Map<String, String> headers;
    private byte[] body;

    private HttpResponse(Builder builder) {
        this.status = builder.status;
        this.headers = new HashMap<>(builder.headers);
        this.body = builder.body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBodyToString() {
        return new String(body);
    }

    public byte[] getHeaderBytes() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("HTTP/1.1 ").append(status).append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        headerBuilder.append("\r\n");

        return headerBuilder.toString().getBytes();
    }

    public byte[] getBodyBytes() {
        return body;
    }

    public byte[] getHttpResponseBytes() {
        byte[] headerBytes = getHeaderBytes();
        byte[] bodyBytes = getBodyBytes();
        byte[] responseBytes = new byte[headerBytes.length + bodyBytes.length];

        System.arraycopy(headerBytes, 0, responseBytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, responseBytes, headerBytes.length, bodyBytes.length);

        return responseBytes;
    }

    public void setContentType(String contentType) {
        if (headers == null) {
            throw new RuntimeException("You must be construct Object");
        }
        headers.put("ContentType", contentType);
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "status=" + status +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }

    public static class Builder {

        private HttpStatus status;
        private Map<String, String> headers;
        private byte[] body;

        public Builder() {
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder setBody(String body) {
            this.body = body.getBytes();
            return this;
        }

        public Builder setBody(byte[] body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }

    }
}
