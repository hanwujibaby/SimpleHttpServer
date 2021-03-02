package com.wayleynam.http;

import com.wayleynam.constants.HttpMethod;
import org.apache.commons.fileupload.FileItem;

import java.io.InputStream;
import java.util.Map;

public interface HttpRequest extends HttpMessage {
    HttpMethod getMethod();

    String getUri();

    String getQueryString();

    String getParameter(String name);

    Map<String, String> getParametersMap();

    InputStream getInputStream();

    String getClientIp();

    int getContentLength();

    String getContentType();

    String getCharaterEncoding();

    FileItem getFile(String name);

}
