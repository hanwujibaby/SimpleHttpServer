package com.wayleynam.http;

import com.wayleynam.constants.HttpVersion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * Http消息抽象接口，HttpRequest和HttpResponse都继承了该接口
 */
public interface HttpMessage {

    String getHeader(String header);

    List<String> getHeaders(String name);

    List<Map.Entry<String, String>> getHeaders();

    boolean containsHeader(String header);


    Set<String> getHeaderNames();

    void addHeader(String name, Object value);


    void setHeader(String name, Object value);

    void setHeader(String name, Iterator<?> values);

    void removeHeader(String name);

    HttpVersion getProtocolVersion();

}
