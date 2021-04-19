package com.wayleynam.http;

import com.wayleynam.utils.ServerConfig;


/***
 *
 * 类似Servlet,实现HttpReequest和HttpResponse实现该接口即可。
 */
public abstract class HttpMessageHandler {

    public void initialize(ServerConfig serverConfig) {

    }

    public void destroy() {

    }

    public abstract byte[] service(HttpRequest httpRequest, HttpResponse httpResponse) throws Throwable;


}
