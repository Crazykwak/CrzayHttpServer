package http.resolver;

import http.messages.HttpRequest;

public class ImageResolver implements Resolver {
    @Override
    public byte[] handle(HttpRequest httpRequest) {
        return new byte[0];
    }
}
