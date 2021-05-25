package com.wayleynam.core;

import com.wayleynam.http.SocketReadHanler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by wei4liverpool on 9/14/15. socket 在channel之间的流转
 */
public final class SocketSession {

    private Log log = LogFactory.getLog(SocketSession.class);
    private ByteBuffer buffer;
    private AsynchronousSocketChannel socket;
    private long timtout;
    private HttpServer server;
    private SocketReadHanler socketReadHanler;
    private ProtocolProcessor processor;
    private Boolean isClosed = false;
    private InetAddress remoteAddress;

    public SocketSession(AsynchronousSocketChannel socket, HttpServer server) throws IOException {
        this.socket = socket;
        this.timtout = server.getTimeout();
        this.server = server;
        this.socketReadHanler = server.getSocketReadHanler();
        this.remoteAddress=((InetSocketAddress)socket.getRemoteAddress()).getAddress();
        this.buffer=server.borrowObject();
        this.processor=new HttpProecess
    }


    public void read() {
        byteBuffer.clear();
        //cketReadHanler=
    }


}
