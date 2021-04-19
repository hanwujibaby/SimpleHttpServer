package com.wayleynam.http;

public interface ExceptionHandler {
    byte[] resolveException(HttpRequest httpRequest, HttpResponse httpResponse, Throwable t);
}
