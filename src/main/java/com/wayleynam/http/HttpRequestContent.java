package com.wayleynam.http;

import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

public class HttpRequestContent implements RequestContext {
    private HttpRequest request;

    public HttpRequestContent(HttpRequest request) {
        this.request = request;
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharaterEncoding();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }
}
