package com.wayleynam.http;

import com.wayleynam.utils.HttpResponseStatus;

import javax.xml.ws.WebEndpoint;

public interface HttpResponse extends HttpMessage {

    HttpResponseStatus getStatus();

    byte[] getContent();

    void setContent(byte[] content);

    void sendRedirect(String url);

}
