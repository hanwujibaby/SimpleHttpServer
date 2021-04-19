package com.wayleynam.http;

public interface GlobalInterceptor {
    boolean beforeHandler(HttpRequest httpRequest, HttpResponse httpResponse, Object handler) throws Exception;

    void afterHandler(HttpRequest httpRequest, HttpResponse httpResponse, Object handler) throws Exception;
}
