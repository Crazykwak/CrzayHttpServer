package http.resolver;

import http.messages.HttpRequest;
import http.messages.HttpResponse;

public interface Resolver {

    byte[] handle(HttpRequest httpRequest);
}
