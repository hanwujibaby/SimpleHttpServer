package com.wayleynam.http;

import com.wayleynam.core.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;

/**
 * Created by wei4liverpool on 9/11/15. 连接建立的时候需要她;
 */
public class SocketAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private static Log log = LogFactory.getLog(SocketReadHanler.class);
    private HttpServer server;
    private ByteBuffer buffer;
    private AsynchronousSocketChannel socket;
    private long timeout;
    private SocketReadHandler socketReadHandler;
    private ProtocolProcessor processor;

    private boolean isClosed = false;

    private InetAddress remoteAddress;

    public SocketAcceptHandler(HttpServer httpServer) {
        this.server = server;
    }

    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {
        log.info("connection accepted");

        this.server.accept();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}
