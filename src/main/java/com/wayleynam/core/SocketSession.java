package com.wayleynam.core;

import com.wayleynam.http.SocketReadHanler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by wei4liverpool on 9/14/15. socket 在channel之间的流转
 */
public class SocketSession {

    private Log log = LogFactory.getLog(SocketSession.class);
    private ByteBuffer byteBuffer;
    private AsynchronousSocketChannel asynchronousSocketChannel;
    private HttpServer server;
    private SocketReadHanler socketReadHanler;
    private ProtocolProcessor processor;
    private Boolean isClosed = false;
    private InetAddress inetAddress;

    public SocketSession(HttpServer httpServer) {
        byteBuffer = httpServer.borrowObject();
    }


    public void read() {
        byteBuffer.clear();
        //cketReadHanler=
    }


}
