package com.wayleynam.http;

import com.wayleynam.constants.HttpVersion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultHttpMessage implements HttpMessage {

    @Override
    public String getHeader(String header) {
        return null;
    }

    @Override
    public List<String> getHeaders(String name) {
        return null;
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return null;
    }

    @Override
    public boolean containsHeader(String header) {
        return false;
    }

    @Override
    public Set<String> getHeaderNames() {
        return null;
    }

    @Override
    public void addHeader(String name, Object value) {

    }

    @Override
    public void setHeader(String name, Object value) {

    }

    @Override
    public void setHeader(String name, Iterator<?> values) {

    }

    @Override
    public void removeHeader(String name) {

    }

    @Override
    public HttpVersion getProtocolVersion() {
        return null;
    }
}
