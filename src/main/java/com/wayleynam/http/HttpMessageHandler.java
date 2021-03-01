package com.wayleynam.http;

import com.wayleynam.utils.ServerConfig;

public abstract class HttpMessageHandler {

    public void initialize(ServerConfig serverConfig){

    }

    public void destroy(){

    }

    public abstract byte[] service(HttpRequest httpRequest,HttpResponse httpResponse) throws Throwable
        ;
}
