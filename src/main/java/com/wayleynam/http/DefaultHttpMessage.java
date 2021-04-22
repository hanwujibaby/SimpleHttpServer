package com.wayleynam.http;

import com.wayleynam.constants.HttpVersion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultHttpMessage implements HttpMessage {
    protected final HttpVersion version;
    protected final HttpHeaders headers = new HttpHeaders();

    public DefaultHttpMessage(HttpVersion version) {
        this.version = version;
    }

    @Override
    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return headers.getHeaders(name);
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return headers.getHeaders();
    }

    @Override
    public boolean containsHeader(String header) {
        return headers.containsHeader(header);
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.getHeaderNames();
    }

    @Override
    public void addHeader(String name, Object value) {
        headers.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, Object value) {
        headers.setHeader(name, value);

    }

    @Override
    public void setHeader(String name, Iterator<?> values) {
        headers.setHeader(name, values);

    }

    @Override
    public void removeHeader(String name) {
        headers.removeHeader(name);
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return version;
    }

    @Override
    public void clearHeaders() {
        headers.clearHeaders();
    }
}
