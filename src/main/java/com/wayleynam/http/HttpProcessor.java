package com.wayleynam.http;

import com.wayleynam.core.HttpServer;
import com.wayleynam.core.SocketSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.text.SimpleDateFormat;

public class HttpProcessor implements Runnable, ProtocolProccesor, CompletionHandler<Integer, ByteBuffer> {

    private Logger logger = LoggerFactory.getLogger(HttpProcessor.class);
    private final SocketSession session;
    private final HttpMessageHandler dynamicHandler;
    private final StaticHandler staticHandler;
    private final HttpMessageSerializer serializer;
    private final HttpServer server;
    private final SimpleDateFormat dateFormat;

    private final String htmlContentType;
    private final int cacheControlMaxAge;
    private DefaultHttpRequest request;
    private boolean keepAlive=false;

    private HttpSocketStatus socketStatus;

    private ByteBuffer byteBuffer;

    public HttpProcessor(SocketSession session, HttpServer server) {
        this.session = session;
        this.server = server;
        this.staticHandler=server.get

    }

    @Override
    public void process() {

    }

    @Override
    public void close() {

    }

    @Override
    public void run() {

    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}
